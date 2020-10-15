package kubernetes

import (
	"fmt"
	"github.com/briandowns/spinner"
	"os"
	"strconv"
	"time"
	"titan/internal/app/clients"
)

func Uninstall(force bool, removeImages bool, context string, port int) {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)
	docker := clients.Docker(context, port)

	available, _ :=  docker.TitanServerIsAvailable()
	if available {
		repos, _, _ := repositoriesApi.ListRepositories(ctx)
		for _, repo := range repos {
			if !force {
				fmt.Println("repository" + repo.Name + "exists, remove first or use '-f'")
				os.Exit(1)
			}
		}
		docker.Remove("titan-" + context + "-server", true)
	}

	s := spinner.New(spinner.CharSets[9], 100*time.Millisecond)
	s.HideCursor = true

	s.Prefix = "Removing Titan Docker volume "
	s.FinalMSG = "Titan Docker volume removed"
	s.Start()
	docker.RemoveVolume("titan-" + context + "-date", true)
	s.Stop()
	fmt.Println()

	if removeImages {
		s.Prefix = "Removing Titan Docker image "
		s.FinalMSG = "Titan Docker image removed"
		s.Start()
		docker.RemoveTitanImages("latest")
		s.Stop()
		fmt.Println()
	}
	fmt.Println("Uninstalled titan infrastructure")
}