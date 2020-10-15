package local

import (
	"fmt"
	"titan/internal/app/clients"
)

func Start(repo string, port int) {
	docker := clients.Docker("", port)
	docker.Start(repo)
	fmt.Println(repo + " started")
}
