package common

import (
	"fmt"
	"strconv"
)

func RemoteRemove(repo string, remote string, port int) {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)

	remotesApi.DeleteRemote(ctx, repo, remote)
	fmt.Println("Removed " + remote + " from " + repo)
}
