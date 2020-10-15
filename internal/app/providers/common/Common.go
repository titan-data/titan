package common

import (
	"context"
	"fmt"
	client "github.com/titan-data/titan-client-go"
	"os"
	"strings"
)

func init() {
	_, d := os.LookupEnv("TITAN_DEBUG")
	cfg.Debug = d
}

var cfg = client.NewConfiguration()
var apiClient = client.NewAPIClient(cfg)
var commitsApi = apiClient.CommitsApi
var operationsApi = apiClient.OperationsApi
var remotesApi = apiClient.RemotesApi
var repositoriesApi = apiClient.RepositoriesApi
var volumesApi = apiClient.VolumesApi
var ctx = context.Background()

func ifContainsPrint(m map[string]interface{}, k string) {
	v, ok := m[k]
	if ok {
		out := fmt.Sprintf("%v: %v", strings.Title(k), v)
		fmt.Println(out)
	}
}