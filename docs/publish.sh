#!/bin/bash

#
# This script handles publishing the previously built docs into a clone of
# the titan-data.github.io repository. It takes the following arguments:
#
# ./publish.sh [-v version] [-f] [-d] [-l] <path>
#
#     -v version  Specify the version to use. Defaults to "development".
#
#     -f          Force rebuild. By default, the script will look at the last
#                 commit hash to touch the docs directory, and skip updating
#                 the docs if the previous version used the same hash.
#
#     -d          Dry run. This will update the contents of the docs site,
#                 but won't commit the result.
#
#     -l          Update latest version to point to current verison.
#
#     path        Path to the root of the titan-data.github.io repository.
#

set -xe

WORKING_DIR=$(realpath $PWD)
DOCS_DIR=$(realpath $(dirname $0))
BUILD_DIR=$DOCS_DIR/build
SRC_DIR=$BUILD_DIR/out

function usage() {
  echo "Usage: $0 [-v version] [-f] [-d] path" 1>&2
  exit 2
}

function die() {
  echo $* 1>&2
  exit 1
}

version=development
force=false
dry_run=false
update_latest=false

while getopts ":fdlv:" o; do
  case "${o}" in
    d)
      dry_run=true
      ;;
    f)
      force=true
      ;;
    l)
      update_latest=true
      ;;
    v)
      version=$OPTARG
      ;;
    *)
      usage
      ;;
  esac
done

shift $((OPTIND-1))
dest=$(realpath $1)

[[ -d $dest ]] || die "Missing or invalid destination directory"

VERSION_DIR=$dest/docs/version

#
# Check to see if we need to rebuild the docs.
#
function check_hash() {
  local vers=$1
  cd $WORKING_DIR
  CURRENT_HASH=$(git log --pretty=format:%H -n 1 $DOCS_DIR)
  if [[ $force = false ]]; then
    if [[ -f $VERSION_DIR/$vers/hash ]]; then
      local previous_hash=$(cat $VERSION_DIR/$vers/hash)

      if [[ $previous_hash = $CURRENT_HASH ]]; then
        echo "Content hasn't changed with hash $previous_hash, skipping"
        exit 0
      fi
    fi
  fi
}

#
# Copy over our source, with hash, and add to git
#
function copy_docs() {
  local vers=$1
  local dst_dir=$VERSION_DIR/$vers
  cd $WORKING_DIR
  if [[ -d $dst_dir ]]; then
    cd $VERSION_DIR && git rm -rf --ignore-unmatch $vers
  fi
  cd $WORKING_DIR
  mkdir -p $VERSION_DIR
  rm -rf $dst_dir
  cp -r $SRC_DIR $dst_dir
  echo $CURRENT_HASH > $dst_dir/hash
  cd $dst_dir && git add .
}

#
# Generate docs.yml data
#
function generate_config() {
  cd $dest
  DOCS_DATA=_data/docs.yml
  if [[ $update_latest = true ]]; then
    latest=$version
  else
    current_latest=$(grep "^latest: " $DOCS_DATA)
    latest=${current_latest#latest: }
  fi
  echo "latest: $latest" > $DOCS_DATA
  echo "versions:" >> $DOCS_DATA
  for v in $(ls -1 $VERSION_DIR | sort -r --version-sort); do
    [[ $v != "development" && $v != "latest" ]] && echo "  - $v" >> $DOCS_DATA
  done
  git add $DOCS_DATA
}

function commit() {
   cd $dest
   git commit -m "docs build $CURRENT_HASH"
   git status
}

check_hash $version
copy_docs $version
[[ $update_latest = true ]] && copy_docs latest
generate_config
commit
