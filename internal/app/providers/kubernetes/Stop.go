package kubernetes

import (
	"fmt"
	"strconv"
)

func Stop(repoName string, port int) {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)

	repo, _, _ := repositoriesApi.GetRepository(ctx, repoName)
	if !repo.Properties["disablePortMapping"].(bool) {
		fmt.Println("Stopping port forwarding")
		k8s.StopPortForwarding(repoName)
	}

	fmt.Println("Updating deployment")
	k8s.StopStatefulSet(repoName)

	fmt.Println("Waiting for deployment to stop")
	k8s.WaitForStatefulSet(repoName)

	fmt.Println("Stopped " + repoName)
}