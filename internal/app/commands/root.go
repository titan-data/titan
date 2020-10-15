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
  "os/user"
  "titan/internal/app/providers"
)

var  (
  context string
  provider providers.Provider
  version string
  verbose bool
  force bool
  guid string
  tags []string
  params []string
  envVars []string
  name string
  source string
  remote string
  updateOnly bool
  removeImages bool
)

// rootCmd represents the base command when called without any subcommands
var rootCmd = &cobra.Command{
  Use:   "titan",
  Short: "Titan CLI",
  Long: `Titan CLI`,
}


// Execute adds all child commands to the root command and sets flags appropriately.
// This is called by main.main(). It only needs to happen once to the rootCmd.
func Execute() {
  if err := rootCmd.Execute(); err != nil {
    fmt.Println(err)
    os.Exit(1)
  }
}

func init() {
  cobra.OnInitialize(initConfig)

  //Global params
  rootCmd.PersistentFlags().StringVar(&context, "context","", "Titan Provider Context")
  rootCmd.Version = "0.5.0"
}

// initConfig reads in config file and ENV variables if set.
func initConfig() {
  u, _ := user.Current()
  titanConfig := u.HomeDir + "/.titan/config"
  if _, err := os.Stat(titanConfig); os.IsNotExist(err) {
    os.Create(titanConfig)
  }
  isInstall := false
  for _, item := range os.Args {
    if item == "install" || item == "ls" {
      isInstall = true
    }
  }
  if context != "" {
    provider, name = providers.ByName(context)
  } else if os.Getenv("TITAN_CONTEXT") != "" {
    provider, name = providers.ByName(os.Getenv("TITAN_CONTEXT"))
  } else if isInstall {
    context = "docker" //TODO confirm valid
  } else {
    provider = providers.Default()
  }
}

