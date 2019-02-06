# Agent test server

## Background
This service is built to enable local agent tests that require *supplemental information*.  bazel tests close `stdin` which has previously been used to query the developer for supplemental information.
The agent tests use a `AgentTestContext` which is configured to communicate with this service when the agent asks for supplemental information.

This service then prompts the developer for said information and hands it back to the `AgentTestContext`.

## Install

(How to install virtualenv and how to use it can be found [here](http://docs.python-guide.org/en/latest/dev/virtualenvs/#lower-level-virtualenv))

```
$ virtualenv .venv
$ source .venv/bin/activate
(.venv) $ python --version
Python 2.7.9
(.venv) $ pip install -r requirements.txt
```

## Start the server
```
$ source .venv/bin/activate
(.venv) $ python agent_test_server.py
Agent test server listening on 127.0.0.1:7357
```

OR

```
$ ./run.sh
Agent test server listening on 127.0.0.1:7357
```
