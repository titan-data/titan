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
	path string
	finalize bool
)

// upgradeCmd represents the upgrade command
var upgradeCmd = &cobra.Command{
	Use:   "upgrade",
	Short: "Upgrade titan CLI and infrastructure",
	Run: func(cmd *cobra.Command, args []string) {
		provider.Upgrade(force, version, finalize, path)
	},
}

func init() {
	rootCmd.AddCommand(upgradeCmd)
	upgradeCmd.Flags().BoolVarP(&force,"force", "f", false, "destroy all repositories")
	upgradeCmd.Flags().StringVarP(&path,"path", "p", "", "full installation path of Titan")
	upgradeCmd.Flags().BoolVar(&force,"finalize", false,  "")
	upgradeCmd.Flags().SortFlags = false
	upgradeCmd.Flags().MarkHidden("finalize")
}
