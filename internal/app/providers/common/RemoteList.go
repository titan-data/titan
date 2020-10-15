package common

import (
	"fmt"
	"github.com/titan-data/remote-sdk-go/remote"
	"strconv"
)

func RemoteList(repo string, port int)  {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)

	remotes, _, _ := remotesApi.ListRemotes(ctx, repo)
	fmt.Printf("%-20s %-20s\n", "REMOTE", "URI") //TODO get proper os line separator
	for _, r := range remotes {
		url, _, _ := remote.Get(r.Provider).ToURL(r.Properties)
		fmt.Printf("%-20s %-20s\n", r.Name, url)
	}
}