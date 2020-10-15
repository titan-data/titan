package local

import (
	"fmt"
	client "github.com/titan-data/titan-client-go"
	"os"
	"strconv"
	"strings"
	"titan/internal/app/clients"
)

func Run(container string, repository string, envVars []string, args []string, disablePortMap bool, createRepo bool, port int, context string) (string, error) {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)
	docker := clients.Docker(context, port)

	if repository != "" && strings.Contains(repository, "/") {
		fmt.Println("Repository name cannot contain a slash")
		os.Exit(1)
	}

	var containerName string
	if repository == "" {
		containerName = container
	} else {
		containerName = repository
	}
	containerExists, _ := docker.ContainerExists(containerName) //TODO handle this error
	if containerExists {
		fmt.Println("Container '" + containerName + "' already exists, name must be unique")
		os.Exit(1)
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

	fmt.Println("Creating repository " + containerName)
 	repo := client.Repository{
		Name:      containerName,
		Properties: nil,
	}
	if createRepo {
		_, _, err := repositoriesApi.CreateRepository(ctx, repo)
		if err != nil && err.Error() == "409 Conflict" {
			fmt.Println("repository '" + repo.Name + "' already exists")
			os.Exit(1)
		}
	}

	argList := []string{"-d", "--label", "io.titandata.titan"}
	var metaVols []map[string]string
	for i, path := range vols {
		volName := containerName + "/v" + strconv.Itoa(i)
		path := strings.Split(path, ":")[0]
		path = strings.ReplaceAll(path, `"`, "")

		fmt.Println("Creating docker volume " + volName + " with path " + path)
		docker.CreateVolume(volName, path)
		argList = append(argList, "--mount")
		argList = append(argList, "type=volume,src="+volName+",dst="+path+",volume-driver=titan-"+docker.GetIdentity())
		addVol := make(map[string]string)
		addVol["name"] = "v" + strconv.Itoa(i)
		addVol["path"] = path
		metaVols = append(metaVols, addVol)
	}
	for i, n := range args{
		if "--name" == n {
			args = append(args[:i], args[i+2:]...)
		}
		if image + ":" + tag == n{
			args = append(args[:i], args[i+1:]...)
		}
	}
	argList = append(argList, args...)
	argList = append(argList,"--name")
	argList = append(argList,containerName)

	var metaPorts []map[string]string
	ports := docker.GetSliceFromImage(image + ":" + tag, "Config", "ExposedPorts")
	for _, rawPort := range ports {
		rawPort = strings.ReplaceAll(rawPort, `"`, "")
		port := strings.Split(rawPort, "/")[0]
		protocol := strings.Split(strings.Split(rawPort, "/")[1], ":")[0]
		if !disablePortMap {
			argList = append(argList, "-p")
			argList = append(argList, port + ":" + port + "/" + protocol)
		}
		addPort := make(map[string]string)
		addPort["protocol"] = protocol
		addPort["port"] = port
		metaPorts = append(metaPorts, addPort)
	}

	for _, env := range envVars {
		argList = append(argList, "--env")
		argList = append(argList, env)
	}

	repoDigest := docker.GetValFromImage(image + ":" + tag, "RepoDigests")
	repoDigest = strings.ReplaceAll(repoDigest, "[", "")
	repoDigest = strings.ReplaceAll(repoDigest, "]", "")
	repoDigest = strings.ReplaceAll(repoDigest, " ", "")
	repoDigest = strings.ReplaceAll(repoDigest, `"`, "")
	repoDigest = strings.TrimSpace(repoDigest)

	var dockerRunCmd string
	if len(repoDigest) == 0 {
		dockerRunCmd = image + ":" + tag
	} else {
		dockerRunCmd = repoDigest
	}

	metadata := map[string]interface{}{
		"v2" : map[string]interface{}{
			"image": map[string]interface{}{
				"image": image,
				"tag": tag,
				"digest": repoDigest,
			},
			"environment": envVars,
			"ports": metaPorts,
			"volumes": metaVols,
		},
	}

	updateRepo := client.Repository{
		Name:       containerName,
		Properties: metadata,
	}
	_, _, err = repositoriesApi.UpdateRepository(ctx, containerName, updateRepo)
	if err != nil {
		fmt.Println(err)
		os.Exit(1)
	}
	_, err = docker.Run(dockerRunCmd, "", argList)

	/**
	The output from Run is used by the CLI and Clone, so the status and message need to ba passed up and handled.
	*/
	var m string
	if err != nil {
		m = err.Error()
	} else {
		m = "Running controlled container " + containerName
	}
	return m, err
}