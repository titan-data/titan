package common

import (
	titanclient "github.com/titan-data/titan-client-go"
	"strconv"
	"strings"
)

func TagCommit(repo string, guid string, tags []string, port int) {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)

	commit, _, _ := commitsApi.GetCommit(ctx, repo, guid)
	commitTags := make(map[string]string)
	t, ok := commit.Properties["tags"]
	if ok {
		commitTags = t.(map[string]string)
	}
	for _, tag := range tags {
		if strings.Contains(tag, "=") {
			s := strings.Split(tag, "=")
			commitTags[s[0]] = s[1]
		} else {
			commitTags[tag] = ""
		}
	}
	m := make(map[string]interface{})
	m = commit.Properties
	m["tags"] = commitTags
	c := titanclient.Commit{
		Id:         commit.Id,
		Properties: m,
	}
	commitsApi.UpdateCommit(ctx, repo, commit.Id, c)
}
