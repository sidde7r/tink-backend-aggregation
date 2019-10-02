# Provider testing script

This script can be used to test a provider in any environment. The script imitates a customer that 
wants to aggregate data by using one of our providers. The script creates a user and
credentials for that user and triggers the authentication flow and performs a refresh 
on the provider. For instance, it does everything that we are doing by running an 
ice-cream hack.

Some background information: the need for such script raised from the following facts:

a) Due to the new policy enforcement, we cannot use ice-cream hack towards production 
environment to test OB providers. (we can of course test them on staging but it is 
always good to test them on production as well since something can go wrong in production
and not in staging especially due to secrets/certificates related issues)
b) Running such script is (expected to be) easier then running ice-cream hack 
to test a provider
c) We do not have a tool to perform a full test from the customer-point-of-view. 
Thanks to this script, we can for example just use the client ID and clientSecret of 
a customer as a parameter to this script to check how the full pipeline will work for 
the customer.

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
