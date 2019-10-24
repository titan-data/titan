## End to End Tests

*   Download runner from [here](https://github.com/mcred/vexrun/releases)
*   `alias vexrun="java -jar vexrun-VERSION.jar"`
*   Make sure titan and docker are both in PATH


## Getting Started Tests
`vexrun -d ./src/endtoend-test/getting-started`

## S3 Tests
```bash
titan clone s3web://demo.titan-data.io/hello-world/postgres hello-world 
vexrun -f ./src/endtoend-test/remotes/RemoteWorkflowTests.yml -p REMOTE s3 -p URI s3://titan-data-cto/e2etest -p REPO hello-world
```