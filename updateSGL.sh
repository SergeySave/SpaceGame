#!/bin/bash
cd "$( dirname "$0" )"

cd core/$1
zip -rq ./../assets/$2 ./ -x "*.DS_Store"