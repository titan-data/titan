# Titan CLI

#### Table of Contents
1.  [Installation](#installation)
    *   [Requirements](#requirements)
    *   [Local Development](#local-dev)
    *   [Setting Up GraalVM](#set-up-graalvm)
    *   [Native Bytecode Generation](#native-bytecode)
2.  [Links](#links)
3.  [Contributing](#contribute)
4.  [License](#license)

## <a id="installation"></a>Installation

### <a id="requirements"></a>Requirements
*  openjdk 1.8.0_202 (see notes for GraalVM)
*  [GraalVM](https://www.graalvm.org/)

### <a id="local-dev"></a>Local Development
```bash
./mvnw clean install
java -jar ./target/titan-VERSION-jar-with-dependencies.jar
```

### <a id="set-up-graalvm"></a>Setting up GraalVM
*  [Install GraalVM](https://www.graalvm.org/docs/getting-started/#install-graalvm)
*  Set JAVA_HOME to be the openjdk include with GraalVM
*  Add the GraalVM bin directory to your PATH

### <a id="native-bytecode"></a>Native Bytecode Generation
```bash
native-image -cp ${PWD}/target/titan-VERSION-jar-with-dependencies.jar\
    -H:Name=titan\
    -H:Class=io.titandata.titan.Cli\
    -H:+ReportUnsupportedElementsAtRuntime\
    -H:ReflectionConfigurationFiles=${PWD}/config/reflect-config.json\
    -H:ResourceConfigurationFiles=${PWD}/config/resource-config.json\
    --allow-incomplete-classpath\
    --enable-url-protocols=http
``` 

### Build Wrapper
Once the jar is created, native binaries can be built with the following scripts.
```bash
./scripts/build-osx.sh
./scripts/build-linux.sh
```

### Releasing New Versions
The version for the CLI is maintained with the `VERSION` file. Bump the version in this file and then run `./scripts/compile-maven.sh` to update the version in the POM.xml file and build a new versioned jar. Currently, an OSX binary release file needs to be committed to a proper release directory. If you are on OSX, run `./scripts/build-osx.sh` to create this file. CI/CD will handle the rest of the builds. 


## <a id="links"></a>Links
*  [Kotlin](https://kotlinlang.org/)
*  [GraalVM](https://www.graalvm.org/)
*  [Clikt](https://github.com/ajalt/clikt)

## <a id="contribute"></a>Contributing

This project follows the Titan community best practices:

  * [Contributing](https://github.com/titan-data/.github/blob/master/CONTRIBUTING.md)
  * [Code of Conduct](https://github.com/titan-data/.github/blob/master/CODE_OF_CONDUCT.md)
  * [Community Support](https://github.com/titan-data/.github/blob/master/SUPPORT.md)

It is maintained by the [Titan community maintainers](https://github.com/titan-data/.github/blob/master/MAINTAINERS.md)

For more information on how it works, and how to build and release new versions,
see the [Development Guidelines](DEVELOPING.md).

## License

This is code is licensed under the Apache License 2.0. Full license is
available [here](./LICENSE).
