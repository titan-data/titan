#!/bin/bash

header=$(echo -n $GITHUB_TOKEN | base64)
exec git -c http.extraheader="AUTHORIZATION: basic $header" $*
