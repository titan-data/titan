package local

import (
	"fmt"
	"titan/internal/app/clients"
)

func Stop(repo string, port int) {
	docker := clients.Docker("",  port)
	docker.Stop(repo)
	fmt.Println(repo + " stopped")
}
