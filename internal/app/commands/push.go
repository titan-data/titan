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

// pushCmd represents the push command
var pushCmd = &cobra.Command{
	Use:   "push [REPOSITORY]",
	Short: "Push data state to remote",
	Args: cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		repo := args[0]
		provider.Push(repo, guid, remote, tags, updateOnly)
	},
}

func init() {
	rootCmd.AddCommand(pushCmd)
	pushCmd.Flags().StringVarP(&guid, "commit","c", "","commit GUID to push, defaults to latest")
	pushCmd.Flags().StringVarP(&remote, "remote","r", "","name of the remote provider, defaults to origin")
	pushCmd.Flags().BoolVarP(&updateOnly, "update-only", "u", false, "update tags only, do not push data")
	pushCmd.Flags().StringSliceVarP(&tags, "tags", "t", nil, "filter commits to select commit to push")
	pushCmd.Flags().SortFlags = false //TODO review flag sorting
}
