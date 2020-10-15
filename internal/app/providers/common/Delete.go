package common

import (
	"fmt"
	client "github.com/titan-data/titan-client-go"
	"strconv"
	"strings"
)

func DeleteCommit(repo string, commit string, port int)  {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)

	commitsApi.DeleteCommit(ctx, repo, commit)
	fmt.Println(commit + " deleted")
}

func DeleteTags(repo string, commit string, tags []string, port int) {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)

	c, _, _ := commitsApi.GetCommit(ctx, repo, commit)
	cTags := c.Properties["tags"].(map[string]string)
	for _, t := range tags {
		if strings.Contains(t, "=") {
			s := strings.Split(t, "=")
			k := s[0]
			v := s[1]

			val, ok := cTags[k]
			if ok && val == v {
				delete(cTags, k)
			}
		} else {
			delete(cTags, t)
		}
	}
	metadata := Metadata{}.Load(c.Properties)
	metadata.SetTags(cTags)
	cm := client.Commit{
		Id:         c.Id,
		Properties: metadata.ToMap(),
	}
	commitsApi.UpdateCommit(ctx, repo, c.Id, cm)
}