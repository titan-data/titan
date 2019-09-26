#!/bin/bash

#
# This is a simple wrapper around the sphinx build that will install the
# necessary python requirements, set sphinx configuration values, etc. It
# places the output in build/out. The followign options are supported:
#
#   -r release    Set the release type. Must be one of "development" or
#                 "official". The former will place a warning indicating
#                 that the documentation may not be reflective of what's
#                 currently available.
#
#   -v version    Version string to use. Defaults to "latest".
#

set -xe

BUILD_DIR=$(dirname $0)/build
VENV_DIR=$BUILD_DIR/venv
OUT_DIR=$BUILD_DIR/out
SRC_DIR=$(dirname $0)/src

function usage() {
  echo "Usage: $0 [-r development|official] [-v version]" 1>&2
  exit 2
}

release_type=development
version=latest

while getopts ":r:v:" o; do
  case "${o}" in
    r)
      release_type=$OPTARG
      ;;
    v)
      version=$OPTARG
      ;;
    *)
      usage
      ;;
  esac
done


mkdir -p $OUT_DIR

#
# Create python environment
#
if [[ -z $VIRTUAL_ENV ]]; then
  if [[ ! -d $VENV_DIR ]]; then
    virtualenv $VENV_DIR --no-site-packages --python=python3
  fi
  source $VENV_DIR/bin/activate
fi

#
# Install python dependencies
#
pip3 install -r $(dirname $0)/requirements.txt

#
# Run sphinx
#
rm -rf $OUT_DIR
sphinx-build -W --keep-going $SRC_DIR $OUT_DIR -D release_type=$release_type \
  -D version=$version

#
# Sphinx's use of _static and friends is problematic for github pages, which is
# run through jekyll. While we could move to an external system like Netlify,
# or build CI/CD to publish a static site somewhere else, simply renaming these
# directories is sufficient for now.
#
mv $OUT_DIR/_static $OUT_DIR/static
mv $OUT_DIR/_sources $OUT_DIR/sources
find $OUT_DIR -name '*.html' -exec sed -i -e 's/_static/static/g' {} \;
find $OUT_DIR -name '*.html' -exec sed -i -e 's/_sources/sources/g' {} \;
