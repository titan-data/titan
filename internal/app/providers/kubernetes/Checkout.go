package kubernetes

import (
	"fmt"
	"github.com/antihax/optional"
	titanclient "github.com/titan-data/titan-client-go"
	"os"
	"strconv"
	"time"
)

func Checkout(repoName string, guid string, tags []string, port int)  {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)

	var sourceCommit string
	if guid == "" {
		if len(tags) > 0 {
			o := optional.NewInterface(tags)
			l := titanclient.ListCommitsOpts{Tag: o}
			commits, _, _ := commitsApi.ListCommits(ctx, repoName, &l)
			if len(commits) == 0 {
				fmt.Println("no matching commits found")
				os.Exit(1)
			}
			sourceCommit = commits[0].Id
		} else {
			status, _, _ := repositoriesApi.GetRepositoryStatus(ctx, repoName)
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

	status, _, _ := commitsApi.GetCommitStatus(ctx, repoName, sourceCommit)

	if !status.Ready {
		fmt.Println("Waiting for commit to be ready")
		c := true
		for c {
			commitStatus, _, _ := commitsApi.GetCommitStatus(ctx, repoName, sourceCommit)
			if commitStatus.Ready {
				c = false
			}
			time.Sleep(1000)
		}
	}

	fmt.Println("Checkout " + sourceCommit)
	commitsApi.CheckoutCommit(ctx, repoName, sourceCommit)

	fmt.Println("Stopping port forwarding")
	k8s.StopPortForwarding(repoName)

	fmt.Println("Updating deployment")
	vols, _, _ := volumesApi.ListVolumes(ctx, repoName)
	k8s.UpdateStatefulSetVolumes(repoName, vols)

	fmt.Println("Waiting for deployment to be ready")
	k8s.WaitForStatefulSet(repoName)

	fmt.Println("Starting port forwarding")
	k8s.StartPortForwarding(repoName)
}