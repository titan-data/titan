package common

import (
	"fmt"
	"github.com/titan-data/remote-sdk-go/remote"
	client "github.com/titan-data/titan-client-go"
	"os"
	"strconv"
)

func RemoteLog(repo string, remoteName string, tags []string, port int) {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)

	remotes, _, _ := remotesApi.ListRemotes(ctx, repo)
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
		commits, _, err := remotesApi.ListRemoteCommits(ctx, repo, r.Name, p, nil) //TODO proper tags
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
				v, ok := c.Properties["tags"]
				if ok {
					//TODO proper tags
					fmt.Println(v)
					//if (commit.properties.containsKey("tags")) {
					//	val tagInfo = commit.properties.get("tags") as Map<String, String>
					//	if (!tagInfo.isEmpty()) {
					//		print("Tags:")
					//		for ((key, value) in tagInfo) {
					//			print(" ")
					//			if (value != "") {
					//				print("$key=$value")
					//			} else {
					//				print(key)
					//			}
					//		}
					//		println("")
					//	}
					//}
				}
				ifContainsPrint(c.Properties, "message")
			}
		} else {
			fmt.Println( r.Name + " has not been initialized.")
		}
	}
}
