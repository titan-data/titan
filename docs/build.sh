#!/bin/bash

set -xe

BUILD_DIR=$(dirname $0)/build
VENV_DIR=$BUILD_DIR/venv
OUT_DIR=$BUILD_DIR/out
SRC_DIR=$(dirname $0)/src

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
sphinx-build -W --keep-going $SRC_DIR $OUT_DIR
