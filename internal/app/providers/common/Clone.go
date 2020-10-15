package common

import (
	"fmt"
	"github.com/antihax/optional"
	"github.com/titan-data/remote-sdk-go/remote"
	client "github.com/titan-data/titan-client-go"
	"net/url"
	"os"
	"strconv"
	"strings"
	"titan/internal/app/clients"
	"titan/internal/app/providers/local"
)

func Clone(uri string, repo string, guid string, params []string, args []string, disablePortMap bool, tags []string, port int, context string) {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)
	docker := clients.Docker(context, port)

	var parsedUri, _ = url.Parse(uri) //TODO handle err
	var repoName string
	if repo == "" {
		var p = strings.Split(parsedUri.Path, "/")
		repoName = p[len(p)-1]
	} else {
		repoName = repo
	}
	var commitId string
	if guid == "" && parsedUri.Fragment != "" {
		commitId = parsedUri.Fragment
	} else {
		commitId = guid
	}
	repository := client.Repository{
		Name:       repoName,
		Properties: make(map[string]interface{}),
	}
 	plainUri := parsedUri.Scheme + "://" + parsedUri.Host + parsedUri.Path
	if len(parsedUri.Query()) > 0 {
		tag := parsedUri.Query().Get("tag")
		tags = append(tags, tag)
	}
	var err error
	cleanup := false
	_, _ , err = repositoriesApi.CreateRepository(ctx, repository)
	if err != nil && err.Error() == "409 Conflict" {
		removeRepo(repoName, port, context)
	} else {
		cleanup = true
		RemoteAdd(repoName, plainUri, "", nil, port) //TODO fix params
		rm, _, _ := remotesApi.GetRemote(ctx, repoName, "origin")
		gp, _ := remote.Get(rm.Provider).GetParameters(rm.Properties)
		p := client.RemoteParameters{
			Provider:   rm.Provider,
			Properties: gp,
		}
		commit := client.Commit{
			Id:         "id",
			Properties: map[string]interface{}{"foo":"bar"},
		}
		if commitId == "" {
			optTags := optional.NewInterface(tags)
			commitsOpts := &client.ListRemoteCommitsOpts{Tag:optTags}
			remoteCommits, _, _ := remotesApi.ListRemoteCommits(ctx, repoName, rm.Name, p, commitsOpts)
			if len(remoteCommits) == 0 {
				fmt.Println("unable to find any matching commits in remote repository")
				removeRepo(repoName, port, context)
			}
			commit = client.Commit{
				Id:         remoteCommits[0].Id,
				Properties: remoteCommits[0].Properties,
			}
		} else {
			if len(tags) > 0 {
				fmt.Println("tags cannot be specified with commit ID")
			}
			c, _, _ := remotesApi.GetRemoteCommit(ctx, repoName, rm.Name, commitId, p)
			commit = client.Commit{
				Id:        	c.Id,
				Properties: c.Properties,
			}
		}
		metadata := Metadata{}.Load(commit.Properties)
		_, err = docker.InspectImage(metadata.image.Digest)
		if err != nil {
			_, err = docker.Pull(metadata.image.Digest)
			if err != nil {
				fmt.Println("Unable to find image " + metadata.image.Digest + " for " + metadata.image.Image)
				os.Exit(1)
			}
		}
		_, err = docker.Pull(metadata.image.Digest)
		var envs []string
		for _, v := range metadata.environment {
			envs = append(envs, fmt.Sprintf("%v",v))
		}
		m, err := local.Run(metadata.image.Digest, repoName, envs, args, disablePortMap, false, port, context)
		if err == nil {
			fmt.Println(m)
			Pull(repoName, commit.Id, "", make([]string, 0), false, port)
			local.Checkout(repoName, commit.Id, nil, port, context)
			cleanup = false
		}
	}
	if cleanup {
		removeRepo(repoName, port, context)
	}
}

func removeRepo(repoName string, port int, context string) {
	fmt.Println("repository '" + repoName + "' already exists")
	local.Remove(repoName, true, port, context)
	os.Exit(1)
}