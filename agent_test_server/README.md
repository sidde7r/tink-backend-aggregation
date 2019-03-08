# Agent test server

## Background
This service is built to enable local agent tests that require *supplemental information*.  bazel tests close `stdin` which has previously been used to query the developer for supplemental information.
The agent tests use a `AgentTestContext` which is configured to communicate with this service when the agent asks for supplemental information.

This service then prompts the developer for said information and hands it back to the `AgentTestContext`.

## Python 2/3 compatibility
[Since Python 2's EOL has been set to 2020][python_eol], `agent_test_server` has been converted to be compatible with both Python 2 and 3 using [python_future].
 
## Install
### Using pipenv
The preferred way is using [pipenv]: 

```bash
$ pipenv install
```

### Using virtualenv & pip
How to install virtualenv and how to use it can be found [here][virtualenv].

```
$ virtualenv .venv
$ source .venv/bin/activate
(.venv) $ python --version
Python 2.7.9
(.venv) $ pip install -r requirements.txt
```

## Start the server
Easy way:
```
$ ./run.sh
...
Agent test server listening on 127.0.0.1:7357
```

or with Pipenv:
```
$ pipenv run python ./agent_test_server.py
...
Agent test server listening on 127.0.0.1:7357
```

or with Virtualenv:
```
$ source .venv/bin/activate
(.venv) $ python agent_test_server.py
...
Agent test server listening on 127.0.0.1:7357
```

[python_eol]: https://pythonclock.org/
[python_future]: https://python-future.org/
[pipenv]: https://github.com/pypa/pipenv
[virtualenv]: http://docs.python-guide.org/en/latest/dev/virtualenvs/#lower-level-virtualenv
