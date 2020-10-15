VEXRUN_FILE := $(PWD)/utils/vexrun.jar
VEXRUN := java -jar $(VEXRUN_FILE)
TITAN_TARGET := $(PWD)/build/titan
TITAN_BIN := /usr/local/bin/titan
RELEASE_DIR := $(PWD)/release
OS := "macos-latest"

.PHONY: build release darwin linux windows

windows:
	GOOS=windows GOARCH=amd64 go build -o $(RELEASE_DIR)/windows/titan.exe $(PWD)/cmd/titan/titan.go

linux:
	GOOS=linux GOARCH=amd64 go build -o $(RELEASE_DIR)/linux/titan $(PWD)/cmd/titan/titan.go

darwin:
	GOOS=darwin GOARCH=amd64 go build -o $(RELEASE_DIR)/darwin/titan $(PWD)/cmd/titan/titan.go

release: darwin linux windows

build:
	go build -o $(TITAN_TARGET) $(PWD)/cmd/titan/titan.go

link:
	ln -s $(TITAN_TARGET) $(TITAN_BIN)

unlink:
	rm  $(TITAN_BIN)

test-setup:
	curl -Ls https://github.com/mcred/vexrun/releases/download/v0.0.5/vexrun-0.0.5.jar -z $(VEXRUN_FILE) -o $(VEXRUN_FILE)

test-install:
	$(VEXRUN) -f $(PWD)/tests/endtoend/infrastructure/Install.yml

test-uninstall:
	$(VEXRUN) -f $(PWD)/tests/endtoend/infrastructure/Uninstall.yml

test-getting-started:
	$(VEXRUN) -d $(PWD)/tests/endtoend/getting-started

test-tags:
	$(VEXRUN) -d $(PWD)/tests/endtoend/tags

test-db-matrix:
	$(VEXRUN) -f $(PWD)/tests/endtoend/db-matrix/databases.yml

test-docker-context:
	docker pull titandata/nginx-test
	docker tag titandata/nginx-test nginx-test
	$(VEXRUN) -d $(PWD)/tests/endtoend/context/docker

test-s3-workflow:
	$(VEXRUN) -f $(PWD)/tests/endtoend/remotes/s3/s3WorkflowTests.yml

test-ssh-workflow:
	$(VEXRUN) -f $(PWD)/tests/endtoend/remotes/ssh/sshWorkflowTests.yml

test-multi-context:
	$(VEXRUN) -d $(PWD)/tests/endtoend/multi-context

e2e: test-setup test-install test-getting-started test-tags test-docker-context test-s3-workflow test-ssh-workflow test-uninstall test-multi-context