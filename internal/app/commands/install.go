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
	"github.com/spf13/cobra"
	"titan/internal/app/providers"
)

// installCmd represents the install command
var installCmd = &cobra.Command{
	Use:   "install",
	Short: "Install titan infrastructure",

	Run: func(cmd *cobra.Command, args []string) {
		provider = providers.Create(context, contextType, providers.GetAvailablePort())
		provider.Install(nil, verbose) //TODO get properties
		providers.AddProvider(provider)
	},
}

func init() {
	rootCmd.AddCommand(installCmd)
	installCmd.LocalFlags().String("registry","titandata", "Registry URL for titan docker image, defaults to titandata")
	installCmd.Flags().BoolVarP(&verbose, "verbose", "v", false, "Verbose output of Titan Server installation steps.")
}
