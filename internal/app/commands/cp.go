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
	destination string
)

// cpCmd represents the cp command
var cpCmd = &cobra.Command{
	Use:   "cp [REPOSITORY]",
	Short: "Copy data into a repository",
	Args: cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		repo := args[0]
		provider.Copy(repo, "local", source, destination)
	},
}

func init() {
	rootCmd.AddCommand(cpCmd)
	cpCmd.Flags().StringVarP(&source, "source", "s", "", "source location of the files on the local machine (required)")
	cpCmd.Flags().StringVarP(&destination, "destination", "d", "", "destination of the files inside of the container")
	_ = cpCmd.MarkFlagRequired("source")
}
