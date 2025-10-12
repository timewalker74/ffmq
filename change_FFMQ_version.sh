#!/bin/bash

set -euo pipefail

cd "$(dirname "$0")"

if [ $# -eq 0 ]; then
    echo "Usage: $0 <version>"
    exit 1
fi

for pom in `find . -name pom.xml`;
do
    echo "Processing POM : "$pom
    sed -i -e "s/^\\(.*\\)<version>.*<\\/version><!--FFMQ_VERSION-->.*$/\\1<version>$1<\\/version><!--FFMQ_VERSION-->/" "$pom"
done
