# Agent test server

## Background
This service is built to enable local agent tests that require *supplemental information*.  bazel tests close `stdin` which has previously been used to query the developer for supplemental information.
The agent tests use a `AgentTestContext` which is configured to communicate with this service when the agent asks for supplemental information.

This service then prompts the developer for said information and hands it back to the `AgentTestContext`.

For the Swedish market, it also provides a way to get a hold of a QR code for agents that uses autostart token. This can be used to open up bankid.

## Python 2/3 compatibility
[Since Python 2's EOL has been set to 2020][python_eol], `agent_test_server` has been converted to be compatible with both Python 2 and 3 using [python_future].
 
## Install
### Using pipenv
The preferred way is using [pipenv]: 
How to install Python 3 and pipenv can be found [here][python3]:

```bash
$ pipenv install
```

### Using virtualenv & pip
How to install virtualenv and how to use it can be found [here][virtualenv].

```
$ python3 -m pip install --upgrade pip
$ pip3 install virtualenv
$ virtualenv -p python3 .venv
$ source .venv/bin/activate
(.venv) $ python --version
Python 3.7.5
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

[python3]: https://github.com/LambdaSchool/CS-Wiki/wiki/Installing-Python-3-and-pipenv
[python_eol]: https://pythonclock.org/
[python_future]: https://python-future.org/
[pipenv]: https://github.com/pypa/pipenv
[virtualenv]: https://help.dreamhost.com/hc/en-us/articles/115000695551-Installing-and-using-virtualenv-with-Python-3

## How to get a hold of a QR code
If you are testing an agent on the swedish market that uses autostart token, the agent test server also exposes a page that shows a QR code. Use this URL to get a QR code to scan: `https://127.0.0.1:7357/bankid`

## SSL certificate

By default, the server will generate a self-signed certificate every time it runs.
If you access it with a browser, you'll have to add it to your system's trust store every time it's run, which will result in you having a lot of trusted self-signed certificates that were only used once.
To prevent this, you can create your own self-signed certificate and use it instead, so you only have to add it once.

To create the key pair:
```
$ openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -nodes -days 365
````

If you have a `cert.pem` and `key.pem` in this directory, `run.sh` will pick them up automatically.

Otherwise, pass them in `--cert` and `--key` arguments:
```
$ pipenv run python ./agent_test_server.py --cert /path/to/cert.pem --key /path/to/key.pem
```

## Python 3 in macOS Catalina: Fixing the abort trap
At some point in the beta program of macOS Catalina Homebrew’s python 3 broke and only ended up showing an 
“Abort trap: 6” for every command that involved using it. This included pip3 and other tools that were previously 
downloaded and worked as expected.

Quick fix:

```
ln -s /usr/local/Cellar/openssl@1.1/1.1.1d/lib/libcrypto.dylib /usr/local/lib/libcrypto.dylib
ln -s /usr/local/Cellar/openssl@1.1/1.1.1d/lib/libssl.dylib /usr/local/lib/libssl.dylib
```