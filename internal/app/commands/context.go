/*
Copyright © 2019 The Titan Project Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package commands

import (
	"fmt"
	"github.com/spf13/cobra"
	"os"
	"titan/internal/app/providers"
)

var contextType string
var contextName string

// contextCmd represents the context command
var contextCmd = &cobra.Command{
	Use:   "context",
	Short: "Manage titan contexts",
}

// contextInstallCmd represents the contextInstall command
var contextInstallCmd = &cobra.Command{
	Use:   "install",
	Short: "Install a new context",
	Run: func(cmd *cobra.Command, args []string) {
		provider = providers.Create(contextName, contextType, providers.GetAvailablePort())
		provider.Install(nil, verbose) //TODO add params
		providers.AddProvider(provider)
	},
}

// contextUninstallCmd represents the contextUninstall command
var contextUninstallCmd = &cobra.Command{
	Use:   "uninstall [CONTEXTNAME]",
	Short: "Uninstall a context",
	Args: cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		provider, context := providers.ByName(args[0])
		provider.Uninstall(force, false)
		providers.Remove(context)
	},
}

// contextDefaultCmd represents the contextDefault command
var contextDefaultCmd = &cobra.Command{
	Use:   "default [CONTEXTNAME]",
	Short: "Get or set default context",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) == 0 {
			p := providers.Default()
			fmt.Println(p.GetName())
			os.Exit(0)
		}
		context = args[0]
		providers.SetDefault(context)
	},
}

// contextListCmd represents the contextList command
var contextListCmd = &cobra.Command{
	Use:   "ls",
	Short: "List available contexts",
	Run: func(cmd *cobra.Command, args []string) {
		h := fmt.Sprintf("%-20s  %-12s", "NAME", "TYPE")
		fmt.Println(h)
		plist := providers.List()
		if len(plist) > 0 {
			defaultName := providers.DefaultName()
			for context, provider := range plist {
				if context == defaultName {
					context = context + " (*)"
				}
				l := fmt.Sprintf("%-20s  %-12s", context, provider.GetType())
				fmt.Println(l)
			}
		}
	},
}

func init() {
	rootCmd.AddCommand(contextCmd)
	contextCmd.AddCommand(contextInstallCmd)
	contextCmd.AddCommand(contextUninstallCmd)
	contextCmd.AddCommand(contextDefaultCmd)
	contextCmd.AddCommand(contextListCmd)

	contextInstallCmd.Flags().StringVarP(&contextType, "type", "t", "docker", "context type (docker or kubernetes)")
	contextInstallCmd.Flags().StringVarP(&contextName, "name", "n", "docker", "context name, defaults to context type")
	contextInstallCmd.Flags().StringSliceVarP(&params, "parameters", "p", nil, "context specific parameters. key=value format")
	contextInstallCmd.Flags().BoolVarP(&verbose, "verbose", "v", false, "verbose logging")
	contextInstallCmd.Flags().SortFlags = false //TODO review flag sorting

	contextUninstallCmd.Flags().BoolVarP(&force, "force", "f", false, "destroy all repositories")

}
