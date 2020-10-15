package kubernetes

import (
	"fmt"
	"strconv"
)

func List(context string, port int) {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)
	repos, _, _ := repositoriesApi.ListRepositories(ctx)
	for _, repo := range repos {
		var status string
		info, err := k8s.GetStatefulSetStatus(repo.Name)
		if err == nil {
			status = info
		} else {
			status = "detached"
		}
		l := fmt.Sprintf("%-12s  %-20s  %s", context, repo.Name, status)
		fmt.Println(l)
	}
}