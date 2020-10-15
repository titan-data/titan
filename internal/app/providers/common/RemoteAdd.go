package common

import (
	"fmt"
	_ "github.com/titan-data/nop-remote-go/nop"
	"github.com/titan-data/remote-sdk-go/remote"
	_ "github.com/titan-data/s3-remote-go/s3"
	_ "github.com/titan-data/s3web-remote-go/s3web"
	_ "github.com/titan-data/ssh-remote-go/ssh"
	client "github.com/titan-data/titan-client-go"
	"os"
	"strconv"
)

func RemoteAdd(repo string, uri string, remoteName string, params map[string]string, port int) {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)

	var name string
	if remoteName != "" {
		name = remoteName
	} else {
		name = "origin"
	}
	_, _, err := remotesApi.GetRemote(ctx, repo, name)
	if err == nil {
		fmt.Println("remote " + name + " already exists for " + repo)
		os.Exit(1)
	}
	provider, props, _, _, _ := remote.ParseURL(uri, params)
	r := client.Remote{
		Provider:   provider,
		Name:       name,
		Properties: props,
	}
	_, _, _ = remotesApi.CreateRemote(ctx, repo, r)
	m, _, _ := repositoriesApi.GetRepository(ctx, repo)
	metadata := m.Properties
	if metadata == nil {
		metadata = make(map[string]interface{})
	}
	metadata["remote"] = name
	newRepo := client.Repository{
		Name:       repo,
		Properties: metadata,
	}
	_, _, _ = repositoriesApi.UpdateRepository(ctx, repo, newRepo)
}