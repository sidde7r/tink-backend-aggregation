#!/bin/bash

DMGFILE=$1
OUTFILE=$2
mkdir -p tmp
VOLUME=$(hdiutil attach "${DMGFILE}" | tail -1 | awk '{print $3}')
cp -r "${VOLUME}/"*.app tmp
hdiutil detach "${VOLUME}" >/dev/null
cd tmp
zip -r "../${OUTFILE}" *
cd ..
rm -rf tmp
rm "${DMGFILE}"