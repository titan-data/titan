package common

import (
	"fmt"
	"github.com/antihax/optional"
	client "github.com/titan-data/titan-client-go"
	"strconv"
)

func Log(repo string, tags []string, port int) {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)

	first := true
	o := optional.NewInterface(tags)
	opts := client.ListCommitsOpts{Tag:o}
	commits, _, _ := commitsApi.ListCommits(ctx, repo, &opts)

	for _, commit := range commits {
		if !first {
			fmt.Println("")
		} else {
			first = false
		}
		metadata := commit.Properties
		fmt.Println("commit " + commit.Id)
		ifContainsPrint(metadata, "author")
		ifContainsPrint(metadata, "user")
		ifContainsPrint(metadata, "email")
		ifContainsPrint(metadata, "timestamp")

		tags, ok := metadata["tags"].(map[string]interface{})
		if ok {
			fmt.Print("Tags: ")
			for t, v := range tags {
				if len(v.(string)) > 0 {
					fmt.Printf("%v=%v ", t, v)
				} else  {
					fmt.Printf("%v ", t)
				}
			}
			fmt.Println()
		}

		if metadata["message"] != "" {
			out := fmt.Sprintf("\n%v", metadata["message"])
			fmt.Println(out)
		}
	}
}


