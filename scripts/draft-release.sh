#!/usr/bin/env bash

set -e
set -o pipefail

if [[ -z "$GITHUB_TOKEN" ]]; then
  echo "Set the GITHUB_TOKEN env variable."
  exit 1
fi

RELEASE=${GITHUB_REF##*/}
body='{
  "tag_name": "'${RELEASE}'",
  "target_commitish": "master",
  "name": "'${RELEASE}'",
  "body": "Draft release for '${RELEASE}'",
  "draft": true,
  "prerelease": false
}'

#Draft Release
RAW_URL=$(curl -X POST -H "Authorization: token $GITHUB_TOKEN" \
  --data "$body" "https://api.github.com/repos/$GITHUB_REPOSITORY/releases" |
  jq -r '.upload_url')

#Prepare Upload
UPLOAD_URL="${RAW_URL%/*}"

echo $UPLOAD_URL
for file in ${PWD}/releases/*.zip; do
    ${PWD}/scripts/upload_asset.sh "$UPLOAD_URL/assets?name=$(basename $file)" $file
done
