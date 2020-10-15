package kubernetes

import (
	"fmt"
	"strconv"
)

func Start(repoName string, port int) {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)

	repo, _, _ := repositoriesApi.GetRepository(ctx, repoName)
	fmt.Println("Updating deployment")
	k8s.StartStatefulSet(repoName)

	fmt.Println("Waiting for deployment to be ready")
	k8s.WaitForStatefulSet(repoName)

	if !repo.Properties["disablePortMapping"].(bool) {
		fmt.Println("Starting port forwarding")
		k8s.StartPortForwarding(repoName)
	}
}
