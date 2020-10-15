package providers

type Provider interface {
	GetType() string
	//GetProperties() struct{}
	GetName() string
	GetPort() int

	Abort(repo string)
	Checkout(repo string, guid string, tags []string)
	Clone(uri string, repo string, commit string, params []string, arguments []string, disablePortMap bool, tags []string)
	Commit(repo string, message string, tags[]string)
	Copy(repo string, driver string, source string, path string)
	Delete(repo string, commit string, tags []string)
	Install(properties []string, verbose bool)
	List(context string)
	Log(repo string, tags []string)
	Migrate(repo string, name string)
	Pull(repo string, commit string, remoteName string, tags []string, metadataOnly bool)
	Push(repo string, commit string, remoteName string, tags []string, metadataOnly bool)
	RemoteAdd(repo string, uri string, remoteName string, params map[string]string)
	RemoteList(repo string)
	RemoteLog(repo string, remoteName string, tags[]string)
	RemoteRemove(repo string, remote string)
	Remove(repo string, force bool)
	Run(image string, repo string, environments []string, arguments []string, disablePortMap bool)
	Start(repo string)
	Status(repo string)
	Stop(repo string)
	Tag(repo string, commit string, tags []string)
	Uninstall(force bool, removeImage bool)
	Upgrade(force bool, version string, finalize bool, path string)
}
