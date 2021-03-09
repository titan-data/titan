package common

import (
	"fmt"
	"github.com/antihax/optional"
	"github.com/titan-data/remote-sdk-go/remote"
	client "github.com/titan-data/titan-client-go"
	"os"
	"strconv"
)

func getRemotes(repo string, remoteName string) []client.Remote {
	var remotes []client.Remote
	if remoteName != "" {
		r, _, _ := remotesApi.GetRemote(ctx, repo, remoteName)
		remotes = append(remotes, r)
	} else {
		remotes, _, _ = remotesApi.ListRemotes(ctx, repo)
	}
	return remotes
}

func RemoteLog(repo string, remoteName string, tags []string, port int) {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)

	remotes := getRemotes(repo, remoteName)
	if len(remotes) == 0 {
		fmt.Println("remote is not set, run 'remote add' first")
		os.Exit(1)
	}
	first := true
	for _, r := range remotes {
		gp, _ := remote.Get(r.Provider).GetParameters(r.Properties)
		p := client.RemoteParameters{
			Provider:   r.Provider,
			Properties: gp,
		}
		o := optional.NewInterface(tags)
		opts := client.ListRemoteCommitsOpts{Tag:o}
		commits, _, err := remotesApi.ListRemoteCommits(ctx, repo, r.Name, p, &opts)
		if err == nil {
			for _, c := range commits {
				if !first {
					fmt.Println()
				} else {
					first = false
				}
				fmt.Println("Commit " + c.Id)
				ifContainsPrint(c.Properties, "author")
				ifContainsPrint(c.Properties, "user")
				ifContainsPrint(c.Properties, "email")
				ifContainsPrint(c.Properties, "timestamp")
				remoteTags, ok := c.Properties["tags"].(map[string]interface{})
				if ok {
					fmt.Print("Tags: ")
					for t, v := range remoteTags {
						if len(v.(string)) > 0 {
							fmt.Printf("%v=%v ", t, v)
						} else  {
							fmt.Printf("%v ", t)
						}
					}
					fmt.Println()
				}
				ifContainsPrint(c.Properties, "message")
			}
		} else {
			fmt.Println( r.Name + " has not been initialized.")
		}
	}
}
