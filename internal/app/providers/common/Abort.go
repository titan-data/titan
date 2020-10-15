package common

import (
	"fmt"
	"os"
	"strconv"
)

func Abort(repo string, port int){
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)

	var operations, _, _ = operationsApi.ListOperations(ctx, nil) //TODO handle error
	var abortCount = 0
	for _, operation := range operations {
		if operation.State == "RUNNING" {
			fmt.Println("aborting operation " + operation.Id)
			operationsApi.AbortOperation(ctx, operation.Id)
			abortCount++
		}
	}
	if abortCount == 0 {
		fmt.Println("no operation in progress")
		os.Exit(0)
	}
}