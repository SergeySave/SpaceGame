#!/bin/bash
cd "$( dirname "$0" )"

cd core/levelFolder
zip -rq ./../assets/level.sgl ./ -x "*.DS_Store"