#!/bin/bash

set -e

apt-get update
apt-get install lcov --yes

testtargets="//src/..."

./coverage.sh "$testtargets" --upload --disk_cache=/cache
