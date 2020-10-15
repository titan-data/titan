package utils

import (
	"context"
	"fmt"
	"github.com/antihax/optional"
	titanclient "github.com/titan-data/titan-client-go"
	"strconv"
	"time"
)

var cfg = titanclient.NewConfiguration()
var apiClient = titanclient.NewAPIClient(cfg)
var operationsApi = apiClient.OperationsApi
var ctx = context.Background()

type operationMonitor struct {
	repo string
	operation titanclient.Operation
}

func OperationMonitor(r string, o titanclient.Operation) operationMonitor {
	return operationMonitor{
		repo:      r,
		operation: o,
	}
}

func (om operationMonitor) IsTerminal(state string) bool {
	r := state == "FAILED" || state == "ABORT" || state == "COMPLETE"
	return r
}

func (om operationMonitor) Monitor(port int) bool {
	cfg.BasePath = "http://localhost:" + strconv.Itoa(port)

	padLen := 0
	//aborted := false
	state := "START"
	var lastId int32 = 0

	for !om.IsTerminal(state) {
		p := &titanclient.GetOperationProgressOpts{LastId:optional.NewInt32(lastId)}
		entries, _, err := operationsApi.GetOperationProgress(ctx, om.operation.Id, p)
		if err == nil {
			if len(entries) > 0 {
				state = entries[len(entries)-1].Type
			}
			for _, e := range entries {
				if e.Type != "PROGRESS" {
					if e.Message != "" {
						fmt.Println(e.Message)
					}
					padLen = 0
				} else {
					m := e.Message
					if len(m) > padLen {
						padLen = len(m)
					}
					fmt.Printf("\r%s", m[0:(padLen - len(m)+ 1)])
				}
				if e.Id > lastId {
					lastId = e.Id
				}
			}
			time.Sleep(2 * time.Second)
		} else {
			/**
			 * We swallow interrupts and instead translate them to an abort call. The operation may have already
			 * completed, so we swallow any exception there. If the users sends multiple interrupts (e.g.
			 * mashing Ctrl-C), then we let them exit out in case there's something seriously broken on the
			 * server.
			 */
			//TODO catch this error

			//		if (aborted) {
			//			throw e
			//		} else {
			//			try {
			//				operationsApi.deleteOperation(operation.id)
			//			} catch (e: ClientException) {
			//				if (e.code != "NoSuchObjectException") {
			//					throw e
			//				}
			//			}
			//			aborted = true
		}
	}

	var opText string
	if om.operation.Type == "PULL" {
		opText = "Pull"
	} else {
		opText = "Push"
	}
	switch state {
		case "COMPLETE":
			fmt.Println(opText + " completed successfully")
		case "FAILED":
			fmt.Println(opText + " failed")
		case "ABORT":
			fmt.Println(opText + " aborted")
	}
	return state == "COMPLETE"
}