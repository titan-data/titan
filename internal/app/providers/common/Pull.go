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

func Pull(repoName string, guid string, remoteName string, tags []string, metadataOnly bool, port int) {
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
		commit, _, _ = remotesApi.GetRemoteCommit(ctx, repoName, remote.Name, guid, params)
	} else {
		o := optional.NewInterface(tags)
		opts := titanclient.ListRemoteCommitsOpts{Tag: o}
		remoteCommits, _, _ := remotesApi.ListRemoteCommits(ctx, repoName, remote.Name, params, &opts)
		if len(remoteCommits) == 0 {
			fmt.Println("no matching commits found in remote, unable to pull latest")
			os.Exit(1)
		}
		commit = remoteCommits[0]
	}
 	if commit.Id == "" {
 		fmt.Println("remote commit not found")
 		os.Exit(1)
	}
	pullOpts := &titanclient.PullOpts{
		MetadataOnly:     optional.NewBool(metadataOnly),
	}
	op, _, _ := operationsApi.Pull(ctx, repoName, remote.Name, commit.Id, params, pullOpts)

	monitor := util.OperationMonitor(repoName, op)
	if !monitor.Monitor(port) {
		os.Exit(1)
	}
}