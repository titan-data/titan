package kubernetes

import (
	"fmt"
	"github.com/briandowns/spinner"
	"strconv"
	"strings"
	"time"
	"titan/internal/app"
	"titan/internal/app/clients"
)

func Install(latest string, registry string, verbose bool, port int, context string){
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)
	docker := clients.Docker(context, port)

	s := spinner.New(spinner.CharSets[9], 100*time.Millisecond)
	s.HideCursor = true

	fmt.Println("Initializing titan infrastructure")
	fmt.Println("Checking docker installation")

	docker.Version()
	if !docker.TitanLatestIsDownloaded(registry, app.Version{}.FromString(latest)) {
		s.Prefix = "Pulling titan docker image (may take a while) "
		s.FinalMSG = "Latest docker image downloaded"
		s.Start()
		docker.Pull(registry + "/titan:" + latest)
		docker.Tag(registry + "/titan:" + latest, "titan:" + latest)
		docker.Tag(registry + "/titan:" + latest, "titan")
		s.Stop()
		fmt.Println()
	}

	serverAvailable, _ := docker.TitanServerIsAvailable()
	if serverAvailable {
		s.Prefix = "Removing titan server "
		s.FinalMSG = "Old titan server removed"
		s.Start()
		docker.Remove("titan-kubernetes-server", true)
		s.Stop()
	}

	launchAvailable, _ := docker.TitanLaunchIsAvailable()
	if launchAvailable {
		s.Prefix = "Removing stale titan-launch container "
		s.FinalMSG = "Stale titan-launch container removed"
		s.Start()
		docker.Remove("titan-kubernetes-launch", true)
		s.Stop()
	}

	//TODO messages don't persist once spinner is closed

	s.Prefix = "Starting titan server docker containers "
	s.FinalMSG = "Titan CLI successfully installed, happy data versioning :)"
	s.Start()
	out, err := docker.LaunchTitanKubernetesServers()
	if err != nil {
		panic(out)
	}
	s.Stop()

	output := false
	logs := docker.FetchLaunchLogs()
	for _, line := range logs {
		if verbose && output && !strings.Contains(line, "TITAN") {
			fmt.Println(line)
		}
		if strings.Contains(line, "TITAN START") {
			fmt.Println(strings.Replace(line, "TITAN START", "", 1)[21:])
			output = true
		}
		if strings.Contains(line, "TITAN END") {
			output = false
		}
		if strings.Contains(line, "TITAN FINISHED") {
			break
		}
		newLogs := docker.FetchLaunchLogs()
		if len(newLogs) > len(logs) {
			logs = append(logs, newLogs[len(logs):]...)
		}
	}
	fmt.Println()
}
