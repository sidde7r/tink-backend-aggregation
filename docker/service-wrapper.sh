#!/bin/bash
set -e

if [ "$1" = '--start-x' ]; then
    shift
    Xvfb -ac :99 -screen 0 1280x1024x16 &
    export DISPLAY=:99
fi
cd "${SERVICE_DIR}"
java \
    -Xmx${MAX_HEAP_SIZE:-2g} \
    -Xloggc:"${GC_LOG:-/gc-logs/gc.log}" \
    -XX:+UseGCLogFileRotation \
    -XX:NumberOfGCLogFiles=${GC_LOG_FILES:-5} \
    -XX:GCLogFileSize=${GC_LOG_SIZE:-512K} \
    -XX:+PrintGCDetails \
    -XX:+PrintGCDateStamps \
    -XX:-OmitStackTraceInFastThrow \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:OnOutOfMemoryError="kill -9 %p" \
    -XX:HeapDumpPath="${HEAPDUMP_PATH:-/heapdumps}" \
    -Dnetworkaddress.cache.ttl=${DNS_TTL:-60} \
    -jar "${SERVICE_DIR}/${SERVICE_NAME}_service.jar" \
    "$@"
