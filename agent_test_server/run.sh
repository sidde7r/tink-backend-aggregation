#!/usr/bin/env bash

args="$@"
if [ -f cert.pem -a -f key.pem ]; then
    echo "Using provided SSL certificate: cert.pem and key.pem"
    args+=" --cert cert.pem --key key.pem"
fi

if [ -f .venv/bin/activate ]; then
    source .venv/bin/activate && python agent_test_server.py $args ; deactivate
elif [ -x "$(command -v pipenv)" ]; then
    pipenv run python ./agent_test_server.py $args
else
    echo "No pipenv or virtualenv found, check readme."
    exit 1
fi
