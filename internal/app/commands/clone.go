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
)

var (
	disablePortMap bool
)

// cloneCmd represents the clone command
var cloneCmd = &cobra.Command{
	Use:   "clone [URI]", //TODO format usage for variadic args
	Short: "Clone a remote repository to local repository",
	Args: cobra.MinimumNArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		uri := args[0]
		provider.Clone(uri, name, guid, params, args[1:], disablePortMap, tags)
	},
}

func init() {
	rootCmd.AddCommand(cloneCmd)
	cloneCmd.Flags().StringVarP(&name, "name","n", "","optional new name for repository")
	cloneCmd.Flags().StringVarP(&guid, "commit","c", "","commit to checkout")
	cloneCmd.Flags().StringSliceVarP(&params, "parameters", "p", nil, "provider specific parameters. key=value format")
	cloneCmd.Flags().BoolVarP(&disablePortMap, "disable-port-mapping", "P", false, "disable default port mapping from container to localhost")
	cloneCmd.Flags().StringSliceVarP(&tags, "tags", "t", nil, "filter latest commit by tags")
}
