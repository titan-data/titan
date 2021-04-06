package local

import (
	"fmt"
	"github.com/briandowns/spinner"
	"os"
	"strconv"
	"strings"
	"time"
	"titan/internal/app"
	"titan/internal/app/clients"
	"titan/internal/app/utils"
)


var ce = utils.CommandExecutor(60, false)

func zfsInstalled() bool {
	mod, _ := ce.Exec("docker", "run", "alpine:latest", "lsmod")
	for _, l := range strings.Split(mod, "\n") {
		for i, w := range strings.Split(l, " ") {
			if i == 0 && w == "zfs"{
				return true
			}
		}
	}
	return false
}

func Install(latest string, registry string, verbose bool, port int, context string) {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)
	docker := clients.Docker(context, port)


	s := spinner.New(spinner.CharSets[9], 100*time.Millisecond)
	s.HideCursor = true

	fmt.Println("Initializing titan infrastructure")
	fmt.Println("Checking docker installation")

	// Make sure Docker is running or panic
	docker.Version()

	// Check for ZFS
	ce.Exec("docker", "pull", "alpine:latest")
	if !zfsInstalled() {
		// Look for Kernel Version
		var args = []string{"run", "--rm", "-i", "--privileged", "--pid=host", "alpine:latest",
			"nsenter", "-t", "1", "-m", "-u", "-n", "-i", "awk", "{ if ($1 == \"kernel:\") { inKernel = 1; next } if (inKernel == 1 && $1 == \"image:\") { print $2; inKernel = 0; quit } }",
			"/etc/linuxkit.yml"}
		v, err := ce.Exec("docker", args...)
		if err != nil {
			fmt.Println("Unable to locate kernel version")
			os.Exit(1)
		}
		// Install ZFS if kernel matches docker desktop 3.2.0 - 3.2.2
		if strings.TrimRight(v, "\n") == "docker/for-desktop-kernel:4.19.121-77626c0840805a2fe3f986674e9e6c5356a33f0c" {
			fmt.Println("Installing ZFS for Docker Desktop")
			_, err = ce.Exec("docker", "run", "--privileged", "--rm", "mcred/install-zfs:docker-desktop-3.2.2")
			if err != nil {
				fmt.Println("Unable to install ZFS for Docker Desktop")
				fmt.Println(err)
			}
		} else {
			fmt.Println("Titan currently supports Docker Desktop 3.2.2. Please confirm the version of Docker Desktop")
			os.Exit(1)
		}
		// Confirm ZFS
		if zfsInstalled() {
			fmt.Println("ZFS for docker desktop installed")
		} else  {
			fmt.Println("Unable to confirm ZFS is installed for Docker Desktop")
			os.Exit(1)
		}
	}

	if !docker.TitanLatestIsDownloaded(app.Version{}.FromString(latest)) {
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
		docker.Remove("titan-docker-server", true)
		s.Stop()
	}

	launchAvailable, _ := docker.TitanLaunchIsAvailable()
	if launchAvailable {
		s.Prefix = "Removing stale titan-launch container "
		s.FinalMSG = "Stale titan-launch container removed"
		s.Start()
		docker.Remove("titan-docker-launch", true)
		s.Stop()
	}

	//TODO messages don't persist once spinner is closed

	s.Prefix = "Starting titan server docker containers "
	s.FinalMSG = "Titan CLI successfully installed, happy data versioning :)"
	s.Start()
	out, err := docker.LaunchTitanServers()
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