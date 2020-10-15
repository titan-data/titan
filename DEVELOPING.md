# Project Development

For general information about contributing changes, see the
[Contributor Guidelines](https://github.com/titan-data/.github/blob/master/CONTRIBUTING.md).

## How it Works

Titan is written with GoLang,

## Requirements
*  GoLang 1.13.5
*  Make

###Setting up Documentation Building
Please read the details in /docs/README.md. As a prerequisite, you must:

* Ensure that Python3 is installed
* Ensure that virtualenv is installed, if not, execute the following:

```bash
pip install virtualenv
```

## Building
```bash
make build
```

## Testing
Titan testing is handled by a simple e2e framework. Full test suite requires that an SSH Key and AWS CLI are configured.
```bash
make e2e
```


## Releasing
```bash
make release
```