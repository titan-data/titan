package common

import (
	"fmt"
	"github.com/antihax/optional"
	rm "github.com/titan-data/remote-sdk-go/remote"
	titanclient "github.com/titan-data/titan-client-go"
	"os"
	"strconv"
	util "titan/internal/app/utils"
)

func Push(repoName string, guid string, remoteName string, tags []string, metadataOnly bool, port int) {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)

	var name string
	if remoteName == "" {
		name = "origin"
	} else {
		name = remoteName
	}
	_, _, err := remotesApi.ListRemotes(ctx, repoName)
	if err != nil {
		fmt.Println("remote is not set, run 'remote add' first")
		os.Exit(1)
	}
	repoStatus, _, _ := repositoriesApi.GetRepositoryStatus(ctx, repoName)
	if repoStatus.LastCommit == "" {
		fmt.Println("container has no history, run 'commit' to first commit state")
		os.Exit(1)
	}
	remote, _, _ := remotesApi.GetRemote(ctx, repoName, name)
	commit := titanclient.Commit{
		Id:         "id",
		Properties: make(map[string]interface{}),
	}
	p, _ := rm.Get(remote.Provider).GetParameters(remote.Properties)
	params := titanclient.RemoteParameters{
		Provider:   remote.Provider,
		Properties: p,
	}
	if guid != "" {
		if len(tags) > 0 {
			fmt.Println("tags cannot be specified when commit is also specified")
			os.Exit(1)
		}
		commit, _, _ = commitsApi.GetCommit(ctx, repoName, guid)
	} else {
		if len(tags) == 0 {
			commit, _, _ = commitsApi.GetCommit(ctx, repoName, repoStatus.LastCommit)
		} else {
			optTags := optional.NewInterface(tags)
			commitsOpts := &titanclient.ListCommitsOpts{Tag:optTags}
			commits, _, _ := commitsApi.ListCommits(ctx, repoName, commitsOpts)
			if len(commits) == 0 {
				fmt.Println("no matching commits found, unable to push latest")
				os.Exit(1)
			}
			commit = commits[0]
		}
	}
	if commit.Id == "" {
		fmt.Println("no matching commits found, unable to push latest")
		os.Exit(1)
	}
	pushOpts := &titanclient.PushOpts{
		MetadataOnly:     optional.NewBool(metadataOnly),
	}
	op, _, err := operationsApi.Push(ctx, repoName, remote.Name, commit.Id, params, pushOpts)
	if err != nil {
		if e, ok := err.(titanclient.GenericOpenAPIError); ok {
			m := e.Model().(titanclient.ApiError)
			fmt.Println(m.Message)
			os.Exit(1)
		}
	}
	monitor := util.OperationMonitor(repoName, op)
	if !monitor.Monitor(port) {
		os.Exit(1)
	}
}