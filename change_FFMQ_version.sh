#!/bin/sh

if [ "$#" = "0" ]; then
	echo "Usage: $0 <version>"
	exit 1
fi

for pom in `find . -name pom.xml`;
do
	echo "Processing POM : "$pom
	cp $pom $pom.orig || exit 1
	cat $pom.orig | sed -e "s/^\\(.*\\)<version>.*<\\/version><!--FFMQ_VERSION-->.*$/\\1<version>$1<\\/version><!--FFMQ_VERSION-->/" > $pom || exit 1
	rm $pom.orig || exit 1
done
