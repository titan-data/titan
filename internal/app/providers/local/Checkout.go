package local

import (
	"fmt"
	"os"
	"strconv"
	"titan/internal/app/clients"
)

func Checkout(repo string, guid string, tags[]string, port int, context string) {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)
	docker := clients.Docker(context, port)

	var sourceCommit string
	if guid == "" {
		if len(tags) > 0 {
			commits, _, _ := commitsApi.ListCommits(ctx, repo, nil)//TODO pass tags
			if len(commits) == 0 {
				fmt.Println("no matching commits found")
				os.Exit(1)
			}
			sourceCommit = commits[0].Id
		} else {
			status, _, _ := repositoriesApi.GetRepositoryStatus(ctx, repo)
			if status.SourceCommit == "" {
				fmt.Println("no commits present, run 'titan commit' first")
				os.Exit(1)
			}
			sourceCommit = status.SourceCommit
		}
	} else {
		if len(tags) > 0 {
			fmt.Println("tags and commit cannot both be specified")
			os.Exit(1)
		}
		sourceCommit = guid
	}
	fmt.Println("Stopping container " + repo)
	docker.Stop(repo)
	fmt.Println("Checkout " + sourceCommit)
	commitsApi.CheckoutCommit(ctx, repo, sourceCommit)
	fmt.Println("Starting container " + repo)
	docker.Start(repo)
	fmt.Println(sourceCommit + " checked out")
}
