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

// runCmd represents the run command
var runCmd = &cobra.Command{
	Use:   "run [IMAGE]",
	Short: "Create repository and start container",
	Long: `Create repository and start container.
Containers associated with a repository can be launched using context specific
run arguments and passed verbatim using '--' as the flag.

Docker example: 'titan run --disable-port-mapping postgres -- -p 2345:5432'`,
	Args: cobra.MinimumNArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		image := args[0]
		provider, name := providers.ByName(name)
		provider.Run(image, name, envVars, args[1:], disablePortMap)
	},
}

func init() {
	rootCmd.AddCommand(runCmd)
	runCmd.Flags().StringVarP(&name, "name","n", "","optional new name for repository")
	runCmd.Flags().StringSliceVarP(&envVars, "env", "e", nil, "container specific environment variables")
	runCmd.Flags().BoolVarP(&disablePortMap, "disable-port-mapping", "P", false, "disable default port mapping from container to localhost")
	runCmd.Flags().StringSliceVarP(&tags, "tags", "t", nil, "filter latest commit by tags")
	runCmd.Flags().SortFlags = false //TODO review flag sorting
}
