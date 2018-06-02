#!/bin/bash
set -xeu

git tag "$VERSION" -m "Build $VERSION"
git push origin "$VERSION"
