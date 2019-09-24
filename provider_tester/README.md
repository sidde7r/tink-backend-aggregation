# Provider testing script

This script can be used to test a provider in Oxford production environment. 
We plan to extend the script to make it usable in other production environments and
also in staging environments as well.

## Requirements 

- Python3.x

## Initial setup for the script

- First of all, you need to create a Tink app. To do so, visit https://console.tink.com.
You need to create the app there and save clientId and clientSecret for the app, you will need
them to run this script. Also, be sure that the app you create has the following scopes:

```
- authorization:grant
- user:create
- user:write
- accounts:read
- transactions:read
- user:read
- credentials:read
- credentials:refresh,
- credentials:write,
- user:write
```

To have those scopes for your app, contact with access-squad team, give your clientID and this list of scopes to the team.

After you ensure that you have those scopes granted for your app, the following explains how to run the script:

- Go to this folder in Terminal.
- Create virtual environment and install the dependencies by executing the following:

```
virtualenv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

## Running the script

- Activate the virtual environment

```
source .venv/bin/activate
```

- Run the following command:

`python test.py --provider_name <provider_name> --market <market_code> --locale <locale_code> --client_id <client_id> --client_secret <client_secret>`
