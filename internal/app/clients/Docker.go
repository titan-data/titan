package clients

import (
	"github.com/buger/jsonparser"
	"os"
	"strconv"
	"strings"
	"titan/internal/app"
)

const EOL  = "\n"

type docker struct {
	identity string
	port int
}

func Docker(i string, p int) docker {
	if i == "" {
		i = "docker"
	}
	if p == 0 {
		p = 5001
	}
	return docker{i, p}
}

/**
https://yourbasic.org/golang/find-search-contains-slice/
*/
func Find(a []string, x string) int {
	for i, n := range a {
		if x == n {
			return i
		}
	}
	return len(a)
}

/**
https://stackoverflow.com/a/37335777
*/
func RemoveFromSlice(a []string, x string) []string {
	s := Find(a, x)
	return append(a[:s], a[s+1:]...)
}

func (d docker) GetIdentity() string  {
	return d.identity
}

func (d docker) getLocalLaunchArgs() []string {
	return []string{
		"--privileged",
		"--pid=host",
		"--network=host",
		"-d",
		"--restart", "always",
		"--name=titan-"+d.identity+"-launch",
		"-v", "/var/lib:/var/lib",
		"-v", "/run/docker:/run/docker",
		"-v", "/lib:/var/lib/titan-"+d.identity+"/system",
		"-v", "titan-"+d.identity+"-data:/var/lib/titan-"+d.identity+"/data",
		"-v", "/var/run/docker.sock:/var/run/docker.sock",
	}
}

func (d docker) Version() (string, error) {
	return ce.Exec("docker", "-v")
}

func (d docker) ContainerExists(container string) (bool, error) {
	out, err := ce.Exec("docker", "ps", "-a", "-f", "name=^/"+container+`$`, "--format", `"{{.Names}}"`)
	return len(out) > 0, err
}

func (d docker) Pull(image string) (string, error) {
	return ce.Exec("docker", "pull", image)
}

func (d docker) Tag(source string, target string) (string, error) {
	return ce.Exec("docker", "tag", source, target)
}

func (d docker) Remove(container string, force bool) (string, error) {
	var args []string
	args = append(args, "rm")
	if force {
		args = append(args, "-f")
	}
	args = append(args, container)
	return ce.Exec("docker", args...)
}

func (d docker) RemoveStopped(repo string) (string, error) {
	c, _ := ce.Exec("docker", "ps", "-a", "-f", "name=^/"+repo+`$`, "--format", `"{{.ID}}"`)
	c = strings.ReplaceAll(c, EOL, "")
	c = strings.ReplaceAll(c, `"`, "")
	return ce.Exec("docker", "container", "rm", c)
}

func (d docker) RemoveVolume(name string, force bool) (string, error) {
	args := [] string {
		"volume", "rm",
	}
	if force {
		args = append(args, "-f")
	}
	args = append(args, name)
	return ce.Exec("docker", args...)
}

func (d docker) InspectContainer(container string) (string, error) {
	return ce.Exec("docker", "inspect", "--type", "container", container)
}

func (d docker) GetValFromContainer(c string, key ...string) (string, error) {
	key = append([]string{"[0]"}, key...)
	result, err := d.InspectContainer(c)
	out, _, _, _ := jsonparser.Get([]byte(result), key...)
	return string(out), err
}

func (d docker) GetSliceFromContainer(c string, key ...string) []string {
	raw, _ := d.GetValFromContainer(c, key...)
	raw = strings.TrimLeft(raw, "[")
	raw = strings.TrimLeft(raw, "{")
	raw = strings.TrimRight(raw, "}")
	raw = strings.TrimRight(raw, "]")
	raw = strings.ReplaceAll(raw, " ", "") //TODO trimspace
	raw = strings.ReplaceAll(raw, EOL, "")
	out := strings.Split(raw, ",")
	return out
}

func (d docker) InspectImage(image string) (string, error) {
	return ce.Exec("docker", "inspect", "--type", "image", image)
}

func (d docker) GetValFromImage(image string, key ...string) string {
	key = append([]string{"[0]"}, key...)
	result, _ := d.InspectImage(image)
	out, _, _, _ := jsonparser.Get([]byte(result), key...)
	return string(out)
}

func (d docker) GetSliceFromImage(image string, key ...string) []string  {
	raw := d.GetValFromImage(image, key...)
	raw = strings.TrimLeft(raw, "{")
	raw = strings.TrimRight(raw, "}")
	raw = strings.ReplaceAll(raw, " ", "") //TODO trimspace
	raw = strings.ReplaceAll(raw, EOL, "")
	out := strings.Split(raw, ",")
	return out
}

func (d docker) Run(image string, entry string, args []string) (string, error) {
	args = append([]string{"run"}, args...)
	args = append(args, image)
	if len(entry) > 0 {
		args = append(args, strings.Split(entry, " ")...)
	}
	return ce.Exec("docker", args...)
}

func (d docker) FetchLogs (container string) []string  {
	output, _ := ce.Exec("docker", "logs", container)
	lines := strings.Split(output, EOL)
	return lines
}

func (d docker) TitanLatestIsDownloaded(registry string, latest app.Version) bool {
	out, _ := ce.Exec("docker", "images", registry + "/titan", "--format", `"{{.Tag}}"`)
	tags := strings.Split(string(out), EOL)
	for _, item := range tags {
		tag := strings.Trim(item,"\"")
		if tag != "latest" && tag != "" {
			v := app.Version{}.FromString(tag)
			if v.Compare(latest) == 0 {
				return true
			}
		}
	}
	return false
}

func (d docker)ContainerIsRunning(container string) (bool, error) {
	out, err := ce.Exec("docker", "ps", "-f", "name=^/"+container+`$`, "--format", `"{{.Names}}"`)
	return len(out) > 0, err
}

func (d docker) TitanServerIsAvailable() (bool, error) {
	return d.ContainerIsRunning("titan-"+d.identity+"-server")
}

func (d docker) TitanLaunchIsAvailable() (bool, error) {
	return d.ContainerIsRunning("titan-"+d.identity+"-launch")
}

func (d docker) LaunchTitanServers() (string, error) {
	args := d.getLocalLaunchArgs()
	args = append(
		args,
		"-e",
		"TITAN_PORT=" + strconv.Itoa(d.port),
		"-e",
		"TITAN_IMAGE=titan:latest",
		"-e",
		"TITAN_IDENTITY=titan-" + d.identity,
	)
	return d.Run("titan:latest", "/bin/bash /titan/launch", args)
}

func (d docker) getKubernetesLaunchArgs() []string {
	home, _ := os.UserHomeDir()
	kube := home + "/.kube"
	return []string{
		"-d",
		"--restart", "always",
		"--name=titan-"+d.identity+"-server",
		"-v", kube + ":/root/.kube",
		"-v", "titan-"+d.identity+"-data:/var/lib/" + d.identity,
		"-e", "TITAN_CONTEXT=kubernetes-csi",
		"-e", "TITAN_IDENTITY=titan-" + d.identity,
		"-p", strconv.Itoa(d.port) + ":5001",
	}
}

func (d docker) LaunchTitanKubernetesServers() (string, error) {
	config := os.Getenv("TITAN_CONFIG")
	args := d.getKubernetesLaunchArgs()
	if config != "" {
		args = append(args, "-e", "TITAN_CONFIG=" + config)
	}
	return d.Run("titan:latest", "/bin/bash /titan/run", args)
}

func (d docker) FetchLaunchLogs() []string {
	return d.FetchLogs("titan-" + d.identity + "-launch")
}

func (d docker) TeardownTitanServers() (string, error) {
	args := d.getLocalLaunchArgs()
	args = RemoveFromSlice(args,"-d")
	args = RemoveFromSlice(args,"--restart")
	args = RemoveFromSlice(args,"always")
	args = RemoveFromSlice(args,"--name=titan-" + d.identity + "-launch")
	args = append(args, "-e", "TITAN_IDENTITY=titan-" + d.identity, "--rm")
	return d.Run("titan:latest", "/bin/bash /titan/teardown", args)
}

func (d *docker) RemoveTitanImages(version string) (string, error) {
	var imageId, _ = ce.Exec("docker", "images", "titan:" + version, "--format", "{{.ID}}")
	imageId = strings.TrimSuffix(imageId, "\n")
	return ce.Exec("docker", "rmi", imageId, "-f")
}

func (d docker) RemoveTitanServer() (string, error) {
	return d.Remove("titan-" + d.identity + "-server", true)
}

func (d docker) RemoveTitanLaunch() (string, error) {
	return d.Remove("titan-" + d.identity + "-launch", true)
}

func (d docker) RemoveTitanVolume() (string, error) {
	return d.RemoveVolume("titan-" + d.identity + "-data", false)
}

func (d docker) CreateVolume(name string, path string) (string, error) {
	return ce.Exec("docker", "volume", "create", "-d", "titan-" + d.identity, "-o", "path=" + path, name)
}

func (d docker) ListVolumes(repo string) []string {
	var args []string
	var r []string
	args = append(args,
		"volume", "ls", "-f", "driver=titan-docker", "-f", "name=" + repo,
		"--format", "{{.Name}}",
	)
	s, err := ce.Exec("docker", args...)
	if err == nil {
		vols := strings.Split(s, "\n")
		vols = vols[:len(vols)-1]
		for _, v := range vols {
			if strings.Contains(v, repo + "/v") {
				r = append(r, v)
			}
		}
	}
	return r
}


func (d docker) Stop(repo string) (string, error) {
	return ce.Exec("docker", "stop", repo)
}

func (d docker) Start(repo string) (string, error) {
	return ce.Exec("docker", "start", repo)
}

func (d docker) Cp(source string, target string) (string, error) {
	return ce.Exec("docker", "cp", "-a", source + "/.", "titan-" + d.identity + "-server:" + target)
}
