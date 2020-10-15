package local

import (
	"fmt"
	"os"
	"strconv"
	"titan/internal/app/clients"
)

func Remove(repo string, force bool, port int, context string) {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)
	docker := clients.Docker(context, port)

	id, _ := docker.GetValFromContainer(repo, "Id")
	if id != "" {
		if !force {
			r, _ := docker.GetValFromContainer(repo, "State", "Status")
			if r == "running" {
				fmt.Println("container " + repo + " is running, stop or use '-f' to force")
				os.Exit(1)
			}
		}
		fmt.Println("Removing repository " + repo)
		containerRunning, _ := docker.ContainerIsRunning(repo)
		if containerRunning {
			_, _ = docker.Remove(repo, force)
		} else  {
			docker.RemoveStopped(repo)
		}
	}
	volumes, _, _ := volumesApi.ListVolumes(ctx,repo)
	for _, volume := range volumes {
		fmt.Println("Deleting volume " + volume.Name)
		_, err := volumesApi.DeactivateVolume(ctx, repo, volume.Name)
		if err != nil {
			panic(err.Error())
		}
		_, err = docker.RemoveVolume(volume.Name, false)
		if err != nil {
			/*
			 * Docker will sometimes fail to launch a container after the
			 * volume has been created. The container does not exist, but
			 * docker thinks the volume is attached to a container and does
			 * not allow it to be removed. Falling back on the VolumeApi
			 * fixes this condition.
			 */
			volumesApi.DeleteVolume(ctx, repo, volume.Name)
		}
	}

	//TODO double check for docker volumes
	//vols := docker.ListVolumes(repo)
	//for _, v := range vols {
	//	vol, err := docker.RemoveVolume(v, true)
	//	if err != nil {
	//		fmt.Println(vol)
	//	}
	//}

	_, err := repositoriesApi.DeleteRepository(ctx, repo)
	if err != nil {
		panic(err.Error())
	}
	fmt.Println(repo + " removed") //TODO this always prints even if already deleted
}