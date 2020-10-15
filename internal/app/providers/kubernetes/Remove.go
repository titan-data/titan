package kubernetes

import (
	"fmt"
	"strconv"
)

func Remove(repo string, force bool, port int) {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)

	// TODO check running  & force
	// TODO why not working kubernetes.stopPortFowarding(repo)

	k8s.DeleteStatefulSpec(repo)
	vols, _, _ := volumesApi.ListVolumes(ctx, repo)
	for _, volume := range vols {
		volumesApi.DeleteVolume(ctx, repo, volume.Name)
	}
	repositoriesApi.DeleteRepository(ctx, repo)
	fmt.Println(repo + " removed")
}