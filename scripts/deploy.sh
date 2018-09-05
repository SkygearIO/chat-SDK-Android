#!/bin/bash -e
generate_doc ()
{
  generate-android-doc --pwd $PWD --src-dir ./chat/src/main/java --package io.skygear.plugins.chat --dst-dir ./javadoc
  publish-doc --platform android --pwd $PWD  --doc-dir $PWD/javadoc --bucket 'docs.skygear.io' --prefix '/android/chat/reference' --version $1 --distribution-id E31J8XF8IPV2V
}

if [[ -z "$BINTRAY_USER" ]]; then
  echo >&2 "Error: \$BINTRAY_USER is not set"
  exit 1
fi

if [[ -z "$BINTRAY_API_KEY" ]]; then
  echo >&2 "Error: \$BINTRAY_API_KEY is not set"
  exit 1
fi

if [[ -n "$TRAVIS_TAG" && "$TRAVIS_TAG" != "latest" ]]; then
   generate_doc $TRAVIS_TAG
  ./gradlew :chat:bintrayUpload
  ./gradlew :chat_ui:bintrayUpload
fi

if [[ "$TRAVIS_TAG" == "latest" ]]; then
   generate_doc "$TRAVIS_TAG"
fi

if [ -n "$TRAVIS_BRANCH" ]; then
   generate_doc $TRAVIS_BRANCH
fi
