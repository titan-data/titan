#!/bin/bash

OS=$1

#Generate Linux Keyfile
if [ "$OS" = "ubuntu-18.04" ]; then
  ssh-keygen -b 2048 -t rsa -f ./sshKey -q -N ""
fi

#Generate OSX Keyfile
if [ "$OS" = "macos-latest" ]; then
  ssh-keygen -b 2048 -t rsa -f ./sshKey -q -N "" <<<y 2>&1 >/dev/null
fi