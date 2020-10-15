package kubernetes

import (
	"fmt"
	client "github.com/titan-data/titan-client-go"
	"os"
	"strconv"
	"strings"
	"titan/internal/app/clients"
)

func Run(container string, repository string, envVars []string, args []string, disablePortMap bool, createRepo bool, port int, context string) {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)
	docker := clients.Docker(context, port)

	if len(args) > 0 {
		fmt.Println("kubernetes provider doesn't support additional arguments")
		os.Exit(1)
	}

	if repository != "" && strings.Contains(repository, "/") {
		fmt.Println("Repository name cannot contain a slash")
		os.Exit(1)
	}

	var repoName string
	if repository == "" {
		repoName = container
	} else {
		repoName = repository
	}

	var image string
	if strings.Contains(container, ":") {
		image = strings.Split(container, ":")[0]
	} else {
		image = container
	}

	var tag string
	if strings.Contains(container, ":") {
		tag = strings.Split(container, ":")[1]
	} else {
		tag = "latest"
	}

	imageInfo, err := docker.InspectImage(image + ":" + tag)
	if err != nil {
		docker.Pull(image + ":" + tag)
		imageInfo, _ = docker.InspectImage(image + ":" + tag)
	}
	if len(imageInfo) == 0 {
		fmt.Println("Image information is not available")
		os.Exit(1)
	}
	vols := docker.GetSliceFromImage(image + ":" + tag, "Config", "Volumes")
	if len(vols) < 1 {
		fmt.Println("No volumes found for image " + image)
		os.Exit(1)
	}

	fmt.Println("Creating repository " + repoName)
	repo := client.Repository{
		Name:      repoName,
		Properties: nil,
	}
	if createRepo {
		_, _, err := repositoriesApi.CreateRepository(ctx, repo)
		if err != nil && err.Error() == "409 Conflict" {
			fmt.Println("repository '" + repo.Name + "' already exists")
			os.Exit(1)
		}
	}

	var titanVolumes []client.Volume
	var metaVolumes []map[string]string
	for i, path := range vols {
		volName := "v" + strconv.Itoa(i)
		path := strings.Split(path, ":")[0]
		path = strings.ReplaceAll(path, `"`, "")
		fmt.Println("Creating titan volume " + volName + " with path " + path)

		v := client.Volume{
			Name:       volName,
			Properties: map[string]interface{}{"path": path},
			Config:     map[string]interface{}{},
		}
		vol, _, err := volumesApi.CreateVolume(ctx, repoName, v)
		//TODO BAD REQUEST

		if err != nil {
			repositoriesApi.DeleteRepository(ctx, repoName)
			panic(err)
			//TODO REMOVE VOLUME AND EXIT
		}
		titanVolumes = append(titanVolumes, vol)
		addVol := map[string]string{
			"name": "v" + strconv.Itoa(i),
			"path": path,
		}
		metaVolumes = append(metaVolumes, addVol)
	}
	fmt.Println("Waiting for volumes to be ready")
	ready := false
	for !ready {
		ready = true
		for _, v := range titanVolumes {
			s, _, _ := volumesApi.GetVolumeStatus(ctx, repoName, v.Name)
			if !s.Ready {
				ready = false
			}
			if s.Error != "" {
				//TODO REMOVE VOLUMES AND EXIT
				fmt.Println("Error creating volume" + v.Name + ": " + s.Error)
				os.Exit(1)
			}
		}
	}

	repoDigest := docker.GetValFromImage(image + ":" + tag, "RepoDigests")
	repoDigest = strings.ReplaceAll(repoDigest, "[", "")
	repoDigest = strings.ReplaceAll(repoDigest, "]", "")
	repoDigest = strings.ReplaceAll(repoDigest, " ", "")
	repoDigest = strings.ReplaceAll(repoDigest, `"`, "")
	repoDigest = strings.TrimSpace(repoDigest)

	var imageId string
	if len(repoDigest) == 0 {
		imageId = image + ":" + tag
	} else {
		imageId = repoDigest
	}

	metadata := map[string]interface{}{
		"container": imageId,
		"image": image,
		"tag": tag,
		"digest": repoDigest,
		"runtime": map[string]interface{}{},
	}
	updateRepo := client.Repository{
		Name:      repoName,
		Properties: metadata,
	}
	repositoriesApi.UpdateRepository(ctx, repoName, updateRepo)

	var metaPorts []map[string]string
	dockerPorts := docker.GetSliceFromImage(image + ":" + tag, "Config", "ExposedPorts")
	ports := make([]int, len(dockerPorts))
	for _, rawPort := range dockerPorts {
		rawPort = strings.ReplaceAll(rawPort, `"`, "")
		port := strings.Split(rawPort, "/")[0]
		protocol := strings.Split(strings.Split(rawPort, "/")[1], ":")[0]
		addPort := make(map[string]string)
		addPort["protocol"] = protocol
		addPort["port"] = port
		metaPorts = append(metaPorts, addPort)
		portInt, _ := strconv.Atoi(port)
		ports = append(ports, portInt)
	}

	metadata = map[string]interface{}{
		"v2" : map[string]interface{}{
			"image": map[string]interface{}{
				"image": image,
				"tag": tag,
				"digest": repoDigest,
			},
			"environment": envVars,
			"ports": metaPorts,
			"volumes": metaVolumes,
			"disablePortMapping": disablePortMap,
		},
	}

	updateRepo = client.Repository{
		Name:      repoName,
		Properties: metadata,
	}
	repositoriesApi.UpdateRepository(ctx, repoName, updateRepo)

	fmt.Println("Creating " + repoName + " deployment")
	k8s.CreateStatefulSet(repoName, imageId, ports, titanVolumes, envVars)

	fmt.Println("Waiting for deployment to be ready")
	k8s.WaitForStatefulSet(repoName)

	if !disablePortMap {
		fmt.Println("Forwarding local ports")
		k8s.StartPortForwarding(repoName)
	}
}