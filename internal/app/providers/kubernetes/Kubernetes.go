package kubernetes

import (
	"context"
	client "github.com/titan-data/titan-client-go"
	"os"
	"titan/internal/app/clients"
)

func init() {
	_, d := os.LookupEnv("TITAN_DEBUG")
	cfg.Debug = d
}

var cfg = client.NewConfiguration()
var apiClient = client.NewAPIClient(cfg)
var repositoriesApi = apiClient.RepositoriesApi
var operationsApi = apiClient.OperationsApi
var remotesApi = apiClient.RemotesApi
var commitsApi = apiClient.CommitsApi
var volumesApi = apiClient.VolumesApi
var contextApi = apiClient.ContextsApi
var ctx = context.Background()

var k8s = clients.Kubernetes("default")