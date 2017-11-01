#!/usr/bin/env bash

echo "Uploading apk to HockeyApp..."

if [[ "$BUDDYBUILD_VARIANTS" =~ "release" ]]; then
  curl \
    -F "release_type=2" \
    -F "status=2" \
    -F "notify=0" \
    -F "ipa=@$BUDDYBUILD_WORKSPACE/chat_example/build/outputs/apk/chat_example-release.apk" \
    -H "X-HockeyAppToken: $HOCKEYAPP_APPTOKEN" \
    https://rink.hockeyapp.net/api/2/apps/$HOCKEYAPP_APPID/app_versions/upload
  echo "Finished uploading apk to HockeyApp."
else
    echo "Only upload release variants to HockeyApp"
fi
