package providers

import (
	"github.com/spf13/viper"
	"net"
	"os"
	"strings"
)

/**
 * The provider factory is responsible for managing multiple providers (contexts to the user). We keep track of
 * providers in the ~/.titan/config file, which is a YAML file that contains a list of contexts and their
 * configuration. Each provider corresponds to an instance of 'titan-server' running somewhere (currently only
 * the user's laptop). The config file keeps track of:
 *
 *      - The context name
 *      - The context type (kubernetes or local)
 *      - The host (always localhost)
 *      - The port to connect to (defaults to 5001)
 *      - Default indicator
 *
 * Additional configuration, such as the provider type and provider-specific configuration, is stored within
 * the titan-server instance and accessible through the getContext() client method. When a context is created, it
 * can be given a type ("docker" or "kubernetes") as well as context-specific configuration.
 *
 * Each repository is associated with a particular context, and can be referred to as "context/repo", or just
 * "repo" for convenience (if there is only one known context, or no conflicts exists).
 */

var Providers map[string]Provider

type context struct {
	isDefault   bool
	host        string
	port        int
	contextType string
}

func loadContext(r interface{}) context {
	m := r.(map[string]interface{})
	return context{
		isDefault:   m["default"].(bool),
		host:        m["host"].(string),
		port:        m["port"].(int),
		contextType: m["type"].(string),
	}
}

func writeContext(c context) map[string]interface{} {
	m := make(map[string]interface{})
	m["default"] = c.isDefault
	m["host"] = c.host
	m["port"] = c.port
	m["type"] = c.contextType
	return m
}

func init() {
	home, _ := os.UserHomeDir()
	viper.SetConfigName("config")
	viper.SetConfigType("yaml")
	viper.AddConfigPath(home + "/.titan")
	err := viper.ReadInConfig()
	if err != nil {
		// Likely config file does not exists, create one.
		_ = os.Mkdir(home+"/.titan", 0755)
		_, _ = os.Create(home + "/.titan/config")
	}
	Providers = make(map[string]Provider)
	contexts := viper.GetStringMap("contexts")
	for index, item := range contexts {
		context := loadContext(item)
		switch context.contextType {
		case "docker":
			Providers[index] = Local(index, context.host, context.port)
		case "kubernetes":
			Providers[index] = Kubernetes(index, context.host, context.port)
		}
	}
}

func ByName(n string) (Provider, string) {
	var p Provider
	if !strings.Contains(n, "/") {
		p = Default()
	} else {
		s := strings.Split(n, "/")
		for k := range Providers {
			if k == s[0] {
				p = Providers[k]
			}
		}
		n = s[1]
	}
	if p == nil {
		panic("no such context '" + n + "'")
	}
	return p, n
}

func List() map[string]Provider {
	return Providers
}

func GetAvailablePort() int {
	l, err := net.Listen("tcp", ":0")
	if err != nil {
		panic(err)
	}
	p := l.Addr().(*net.TCPAddr).Port
	_ = l.Close()
	return p
}

func Create(name string, provider string, port int) Provider {
	if Providers[name] != nil {
		panic("context '" + name + "' already exists")
	}
	var p Provider
	switch provider {
	case "docker":
		p = Local(name, "localhost", port) //TODO confirm provider host
	case "kubernetes":
		p = Kubernetes(name, "localhost", port)
	}
	return p
}

func AddProvider(p Provider) {
	contexts := viper.GetStringMap("contexts")
	context := context{
		isDefault:   len(contexts) < 1,
		host:        "localhost", //TODO remap host URL
		port:        p.GetPort(),
		contextType: p.GetType(),
	}
	contexts[p.GetName()] = writeContext(context)
	viper.Set("contexts", contexts)
	err := viper.WriteConfig()
	if err != nil {
		panic(err)
	}
}

func Remove(n string) {
	contexts := viper.GetStringMap("contexts")
	current := loadContext(contexts[n])
	delete(contexts, n)
	// If we delete the default provider, just pick first one to be default
	if current.isDefault == true && len(contexts) > 0 {
		for k, c := range contexts {
			context := loadContext(c)
			context.isDefault = true
			contexts[k] = writeContext(context)
			break
		}
	}
	viper.Set("contexts", contexts)
	err := viper.WriteConfig()
	if err != nil {
		panic(err)
	}
}

func SetDefault(n string) {
	contexts := viper.GetStringMap("contexts")
	for k, c := range contexts {
		context := loadContext(c)
		context.isDefault = false
		if k == n {
			context.isDefault = true
		}
		contexts[k] = writeContext(context)
	}
	viper.Set("contexts", contexts)
	err := viper.WriteConfig()
	if err != nil {
		panic(err)
	}
}

func DefaultName() string {
	contexts := viper.GetStringMap("contexts")
	if len(contexts) == 0 {
		panic("No context is configured, run 'titan install' or 'titan context install' to configure titan")
	}
	var name string
	if len(contexts) == 1 {
		for k := range contexts {
			name = k
		}
	} else {
		for k, context := range contexts {
			c := loadContext(context)
			if c.isDefault {
				name = k
				break
			}
		}
	}
	if name == "" {
		panic("More than one context specified, but no default set")
	}
	return name
}

func Default() Provider {
	return Providers[DefaultName()]
}
