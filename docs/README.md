# Titan Technical Documentation

The technical documentation is kept alongside the titan repository, so that
we can easily update it as the implementation evolves. The documentation is
built using sphinx with the readthedocs theme. If you haven't worked with
reStructuredText before, search online for "sphinx documentation" to get
started.

## Building Locally

To build the docs, simply run `./build.sh` within the docs directory. This
will install sphinx dependencies and run `sphinx-build` to place the result
in `build/out`. Open `index.html` in that folder to view the result.

## Releases

These docs are automatically published to [titan-data.io](https://titan-data.io)
via GitHub actions. Every push that affects the `docs` directory will update
the `development` version on the main site, and every tag will update a named
release.
