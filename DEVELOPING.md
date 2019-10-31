# Project Development

For general information about contributing changes, see the
[Contributor Guidelines](https://github.com/titan-data/.github/blob/master/CONTRIBUTING.md).

## How it Works

Titan is written with Kotlin for JVM and binaries are compiled using GraalVM. 

## Requirements
*  openjdk 1.8.0_212 (see notes for GraalVM)
*  [GraalVM](https://www.graalvm.org/)

###Setting up GraalVM
*  [Install GraalVM](https://www.graalvm.org/docs/getting-started/#install-graalvm)
*  Set JAVA_HOME to be the openjdk include with GraalVM
*  Add the GraalVM bin directory to your PATH

## Building
```bash
./mvnw clean install
java -jar ./target/titan-VERSION-jar-with-dependencies.jar
```

Once the jar is created, native binaries can be built with the following scripts.
```bash
./scripts/build-osx.sh
./scripts/build-linux.sh

## Testing

Describe how to test the project.

## Releasing

The version for the CLI is maintained with the `VERSION` file. Bump the version in this file and then run `./scripts/compile-maven.sh` to update the version in the POM.xml file and build a new versioned jar. Currently, an OSX binary release file needs to be committed to a proper release directory. If you are on OSX, run `./scripts/build-osx.sh` to create this file. CI/CD will handle the rest of the builds. 