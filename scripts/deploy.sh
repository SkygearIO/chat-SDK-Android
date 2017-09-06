#!/bin/bash -e

if [[ -z "$BINTRAY_USER" ]]; then
  echo >&2 "Error: \$BINTRAY_USER is not set"
  exit 1
fi

if [[ -z "$BINTRAY_API_KEY" ]]; then
  echo >&2 "Error: \$BINTRAY_API_KEY is not set"
  exit 1
fi

if [ "$TRAVIS_TAG" == "latest" ]; then
    echo $TRAVIS_TAG
fi


publish-doc --platform android --pwd $PWD  --doc-dir $PWD/javadoc --bucket 'docs.skygear.io' --prefix '/android/chat/reference' --version 'latest' --distribution-id E31J8XF8IPV2V

./gradlew :chat:bintrayUpload
