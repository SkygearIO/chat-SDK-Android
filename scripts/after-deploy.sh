#!/bin/bash -e

if [[ "$TRAVIS_BRANCH" == "$DEPLOYMENT_BRANCH" ]]; then
  echo "Invalidating cloudfront caching... "
  npm install void
  node scripts/invalidate-cf
fi
