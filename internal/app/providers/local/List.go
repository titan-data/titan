package local

import (
	"fmt"
	"strconv"
	"titan/internal/app/clients"
)

func List(context string, port int) {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)
	docker := clients.Docker("", port)

	repos, _, _ := repositoriesApi.ListRepositories(ctx)
	for _, repo := range repos {
		var status string
		info, err := docker.GetValFromContainer(repo.Name, "State", "Status")
		if err == nil {
			status = info
		} else {
			status = "detached"
		}
		l := fmt.Sprintf("%-12s  %-20s  %s", context, repo.Name, status)
		fmt.Println(l)
	}
}