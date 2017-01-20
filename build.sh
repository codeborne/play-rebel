#!/bin/bash
ORGANIZATION="play-rebel"
MODULE="rebel"
VERSION=`grep self conf/dependencies.yml | sed "s/.*$MODULE //"`

REPO=/var/www/repo/$ORGANIZATION/$MODULE
TARGET=$REPO/$MODULE-$VERSION.jar

rm -fr dist
rm -fr lib

play dependencies --sync || exit $?
rm -fr .lib || exit $?
mkdir .lib || exit $?
mv lib/* .lib/ || exit $?

play build-module || exit $?

if [[ "$VERSION" == *SNAPSHOT ]]
then
  echo "Skip publishing $TARGET (nobody needs snapshot)"
elif [ -e $TARGET ]; then
  echo "Not publishing ($MODULE-$VERSION already exists)"
elif [ -e $REPO ]; then

  echo ""
  echo ""
  echo ""
  echo " ********************************************************* "
  cp lib/play-rebel.jar $TARGET || exit $?
  echo "Published $TARGET"
  echo " ********************************************************* "
  echo ""
  echo ""
  echo ""

else
  echo "Not publishing ($REPO does not exists)"
fi
