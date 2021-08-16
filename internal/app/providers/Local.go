package providers

import (
	"fmt"
	"os"
	"strings"
	cmn "titan/internal/app/providers/common"
	lcl "titan/internal/app/providers/local"
	"titan/internal/app/utils"
)

var ce = utils.CommandExecutor(60, false)
var user, _ = ce.Exec("git", "config", "user.name")
var email, _ = ce.Exec("git", "config", "user.email")

type local struct{
	contextName string
	host string
	portNum int
	titanServerVersion string
	dockerRegistryUrl string
}

func (l local) GetType() string {
	return "docker"
}

func (l local) GetName() string {
	return l.contextName
}

func (l local) GetPort() int {
	return l.portNum
}

func (l local) Abort(repo string) {
	cmn.Abort(repo, l.portNum)
}

func (l local) Checkout(repo string, guid string, tags []string) {
	lcl.Checkout(repo, guid, tags, l.portNum, l.contextName)
}

func (l local) Clone(uri string, repo string, commit string, params []string, arguments []string, disablePortMap bool, tags []string) {
	cmn.Clone(uri, repo, commit, params, arguments, disablePortMap, tags, l.portNum, l.contextName)
}

func (l local) Commit(repo string, message string, tags []string) {
	cmn.Commit(repo, message, tags, strings.TrimSpace(user), strings.TrimSpace(email), l.portNum)
}

func (l local) Copy(repo string, driver string, source string, path string) {
	lcl.Copy(repo, driver, source, path, l.portNum, l.contextName)
}

func (l local) Delete(repo string, commit string, tags []string) {
	if commit != "" {
		if len(tags) > 0 {
			cmn.DeleteTags(repo, commit, tags, l.portNum)
		} else {
			cmn.DeleteCommit(repo, commit, l.portNum)
		}
	} else {
		fmt.Println("No object found to delete.")
	}
}

func (l local) Install(properties []string, verbose bool) {
	//TODO review properties
	lcl.Install(l.titanServerVersion, l.dockerRegistryUrl, verbose, l.portNum, l.contextName)
}

func (l local) List(context string) {
	lcl.List(context, l.portNum)
}

func (l local) Log(repo string, tags []string) {
	cmn.Log(repo, tags, l.portNum)
}

func (l local) Migrate(repo string, name string) {
	lcl.Migrate(repo, name, strings.TrimSpace(user), strings.TrimSpace(email), cmn.Commit, l.portNum, l.contextName)
}

func (l local) Pull(repo string, commit string, remoteName string, tags []string, metadataOnly bool) {
	cmn.Pull(repo, commit, remoteName, tags, metadataOnly, l.portNum)
}

func (l local) Push(repo string, commit string, remoteName string, tags []string, metadataOnly bool) {
	cmn.Push(repo, commit, remoteName, tags, metadataOnly, l.portNum)
}

func (l local) RemoteAdd(repo string, uri string, remoteName string, params map[string]string) {
	cmn.RemoteAdd(repo, uri, remoteName, params, l.portNum)
}

func (l local) RemoteList(repo string) {
	cmn.RemoteList(repo, l.portNum)
}

func (l local) RemoteLog(repo string, remoteName string, tags []string) {
	cmn.RemoteLog(repo, remoteName, tags, l.portNum)
}

func (l local) RemoteRemove(repo string, remote string) {
	cmn.RemoteRemove(repo, remote, l.portNum)
}

func (l local) Remove (repo string, force bool) {
	lcl.Remove(repo, force, l.portNum, l.contextName)
}

func (l local) Run(image string, repo string, environments []string, arguments []string, disablePortMap bool) {
	s, err := lcl.Run(image, repo, environments, arguments, disablePortMap, true, l.portNum, l.contextName)
	fmt.Println(s)
	if err != nil {
		os.Exit(1)
	}
}

func (l local) Start(repo string) {
	lcl.Start(repo, l.portNum)
}

func (l local) Status(repo string) {
	cmn.Status(repo, l.portNum, l.contextName)
}

func (l local) Stop(repo string) {
	lcl.Stop(repo, l.portNum)
}

func (l local) Tag(repo string, commit string, tags []string) {
	cmn.TagCommit(repo, commit, tags, l.portNum)
}

func (l local) Uninstall(force bool, removeImage bool) {
	lcl.Uninstall(l.titanServerVersion, force, removeImage, l.portNum, l.contextName)
}

func (l local) Upgrade(force bool, version string, finalize bool, path string) {
	panic("implement me")
}

func Local(contextName string, host string, port int) Provider {
	return local{
		contextName:contextName,
		host:host,
		portNum:port,
		titanServerVersion: "0.8.3",
		dockerRegistryUrl: "titandata",
	}
}