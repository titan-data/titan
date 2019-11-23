#!/bin/bash

/bin/bash -c printenv > /usr/share/nginx/html/index.html
nginx -g "daemon off;"