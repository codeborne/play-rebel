#!/bin/bash
ORGANIZATION="play-rebel"
MODULE="rebel"
VERSION=`grep self conf/dependencies.yml | sed "s/.*$MODULE //"`
DESTINATION=/var/www/repo/$ORGANIZATION
TARGET=$DESTINATION/$MODULE-$VERSION.zip

rm -fr dist
rm -fr lib

play dependencies --sync || exit $?
rm -fr .lib || exit $?
mkdir .lib || exit $?
mv lib/* .lib/ || exit $?

play build-module || exit $?

if [ -d $DESTINATION ]; then
  if [ -e $TARGET ]; then
      echo "Not publishing, $MODULE-$VERSION already exists"
  else
      cp dist/*.zip $TARGET || exit $?
      echo "Package is available at https://repo.codeborne.com/$ORGANIZATION/$MODULE-$VERSION.zip"
  fi
fi
