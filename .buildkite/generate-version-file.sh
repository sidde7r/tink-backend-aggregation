#!/bin/bash
set -e

DATE=$(date +%s000)
COMMIT=$(git rev-parse HEAD)

echo "--- Set version"
echo "Version: $VERSION"
echo "{\"version\":\"${VERSION}\", \"commit\":\"${COMMIT}\", \"date\":${DATE}}" > data/version.json
echo "${VERSION}" > deb/.version
