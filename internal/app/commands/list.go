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
	"titan/internal/app/providers"
)

// listCmd represents the list command
var listCmd = &cobra.Command{
	Use:   "ls",
	Short: "List repositories",

	Run: func(cmd *cobra.Command, args []string) {
		h := fmt.Sprintf("%-12s  %-20s  %s", "CONTEXT", "REPOSITORY", "STATUS")
		fmt.Println(h)
		for key, provider := range providers.List() {
			provider.List(key)
		}
	},
}

func init() {
	rootCmd.AddCommand(listCmd)
}
