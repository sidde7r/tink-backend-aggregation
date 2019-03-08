#!/usr/bin/env bash

if [ -x "$(command -v pipenv)" ]; then
    pipenv run python ./agent_test_server.py
else
    source .venv/bin/activate && python agent_test_server.py ; deactivate
fi
