#!/usr/bin/env bash

echo "Uploading apk to HockeyApp..."


if [[ "$BUDDYBUILD_BRANCH" == "master" ]]; then
  APK_PATH=($(find . -name *-release.apk))
  curl \
    -F "release_type=2" \
    -F "status=2" \
    -F "notify=0" \
    -F "ipa=@$APK_PATH" \
    -H "X-HockeyAppToken: $HOCKEYAPP_APPTOKEN" \
    https://rink.hockeyapp.net/api/2/apps/$HOCKEYAPP_APPID/app_versions/upload
  echo "Finished uploading apk to HockeyApp."
else
    echo "Only upload release variants to HockeyApp"
fi
