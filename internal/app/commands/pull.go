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


// pullCmd represents the pull command
var pullCmd = &cobra.Command{
	Use:   "pull [REPOSITORY]",
	Short: "Pull a new data state from remote",
	Args: cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		repo := args[0]
		provider.Pull(repo, guid, remote, tags, updateOnly)
	},
}

func init() {
	rootCmd.AddCommand(pullCmd)
	pullCmd.Flags().StringVarP(&guid, "commit","c", "","commit GUID to pull from, defaults to latest")
	pullCmd.Flags().StringVarP(&remote, "remote","r", "","name of the remote provider, defaults to origin")
	pullCmd.Flags().BoolVarP(&updateOnly, "update-only", "u", false, "update tags only, do not pull data")
	pullCmd.Flags().StringSliceVarP(&tags, "tags", "t", nil, "filter commits to select commit to pull")
	pullCmd.Flags().SortFlags = false //TODO review flag sorting
}
