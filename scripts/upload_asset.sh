#!/usr/bin/env bash

set -e
set -o pipefail

UPLOAD_URL=$1
FILE=$2
MIME=$3
SIZE=$(wc -c $FILE | awk '{print $1}')


#Upload Asset
curl \
  -H "Authorization: token $GITHUB_TOKEN" \
  -H "Content-Length: $SIZE"\
  -H "Content-Type: application/$MIME" \
  --data-binary @$FILE "$UPLOAD_URL"