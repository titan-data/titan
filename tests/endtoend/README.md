## End to End Tests

```
make test-e2e
```

## Manual Install

*   Download runner from [here](https://github.com/mcred/vexrun/releases)
*   `alias vexrun="java -jar vexrun-VERSION.jar"`
*   Make sure titan and docker are both in PATH

## Getting Started Tests
```
vexrun -d ./src/endtoend-test/getting-started
```

## S3 Tests
The following environment variables must be set:

* AWS_ACCESS_KEY_ID
* AWS_SECRET_ACCESS_KEY
* AWS_REGION

Alternately, `aws configure` can be used to set up AWS access. 

```bash
titan clone s3web://demo.titan-data.io/hello-world/postgres hello-world 
vexrun -f ./src/endtoend-test/remotes/RemoteWorkflowTests.yml -p REMOTE s3 -p URI s3://titan-data-cto/e2etest -p REPO hello-world
```

## SSH Tests
An SSH Keyfile must be created. The script `generateKey.sh` in the ssh test directory can assist with this. 

