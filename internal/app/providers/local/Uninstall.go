package local

import (
	"fmt"
	"os"
	"strconv"
	"titan/internal/app/clients"
)

func Uninstall(version string, force bool, removeImages bool, port int, context string) {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)
	docker := clients.Docker(context, port)

	serverAvailable, _ := docker.TitanServerIsAvailable()
	if serverAvailable {
		var repos, _, _ = repositoriesApi.ListRepositories(ctx)
		for _, repo := range repos {
			if !force {
				fmt.Println("repository '" + repo.Name + "' exists, remove first or use '-f'")
				os.Exit(1)
			}
			Remove(repo.Name, true, port, context)
		}
	}
	if serverAvailable {
		s, err := docker.RemoveTitanServer()
		if err != nil {
			fmt.Println(s)
			panic(err)
		}
	}
	launchAvailable, _ := docker.TitanLaunchIsAvailable()
	if launchAvailable {
		s, err := docker.RemoveTitanLaunch()
		if err != nil {
			fmt.Println(s)
			panic(err)
		}
	}

	fmt.Println("Tearing down Titan servers")
	docker.TeardownTitanServers() //TODO track this

	fmt.Println("Removing titan-data Docker volume")
	docker.RemoveTitanVolume() //TODO track this

	if removeImages {
		fmt.Println("Removing Titan Docker image")
		docker.RemoveTitanImages(version) //TODO track this
	}
	fmt.Println("Uninstalled titan infrastructure")
}