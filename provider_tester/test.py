import json
from urls import URLConfig
from config import AggregationConfig
from utils import HTTPRequestHelper
from models import *
import uuid
import time
import argparse

parser = argparse.ArgumentParser()
parser.add_argument(
    '--provider_name',
    help='Provider name to be tested (example: dk-ringkjobinglandbobank-ob)'
)

parser.add_argument(
    '--market',
    help='Market code to be used (example: DK)'
)

parser.add_argument(
    '--locale',
    help='Locale to be used (example: da_DK)'
)

parser.add_argument(
    '--client_id',
    help='Client ID of the app'
)

parser.add_argument(
    '--client_secret',
    help='Client Secret of the app'
)

parser.add_argument(
    '--host',
    default="https://api.tink.se/api/v1/",
    help="Host URL of the environment where the provider will be tested (ex: https://api.tink.se/api/v1/)"
)

args = parser.parse_args()
provider_name = args.provider_name
market = args.market
locale = args.locale
client_id = args.client_id
client_secret = args.client_secret

AggregationConfig.host_url = args.host

if provider_name is None:
    raise Exception("[ERROR] Provider name must be provided by --provider_name argument")

if market is None:
    raise Exception("[ERROR] Market code must be provided by --market argument")

if locale is None:
    raise Exception("[ERROR] Locale must be provided by --locale argument")

if client_id is None:
    raise Exception("[ERROR] Client ID must be provided by --client_id argument")

if client_secret is None:
    raise Exception("[ERROR] Client Secret must be provided by --client_secret argument")

AggregationConfig.client_token_request_data["client_id"] = client_id
AggregationConfig.client_token_request_data["client_secret"] = client_secret

http_request_helper = HTTPRequestHelper()

print("[INFO] Executing step 1: Getting client token for communicating with Aggregation")

client_token_response = ClientTokenResponse(
    http_request_helper.make_request(
        url=URLConfig.get_client_token_fetch_url(),
        payload=AggregationConfig.client_token_request_data,
        request_type="POST",
        stringify_payload=True
    ))

print("[INFO] Client token successfully fetched.")

print("[INFO] Executing step 2: Creating a user in Aggregation")
print("[INFO] Information about user to be created")
print("---")

user_data = {
    "market": market,
    "locale": locale
}

print(json.dumps(user_data, indent=1))

http_request_helper = HTTPRequestHelper(client_token_response)

user_create_response = UserCreateResponse(
    http_request_helper.make_request(
        url=URLConfig.get_user_create_request_url(),
        payload=user_data,
        request_type="POST",
        content_type="application/json"
    ))


print("[INFO] User created successfully (userID of the created user = " + user_create_response.user_id + ")")

print("[INFO] Executing step 3: Granting access for the user created in step 2")

url = URLConfig.get_grant_user_access_request_url()
query_params = AggregationConfig.client_token_request_data
payload = AggregationConfig.get_grant_user_access_request_payload(user_id=user_create_response.user_id)

grant_user_access_response = UserAccessResponse(
    http_request_helper.make_request(
        url=URLConfig.get_grant_user_access_request_url(),
        query_params=AggregationConfig.client_token_request_data,
        payload=AggregationConfig.get_grant_user_access_request_payload(user_id=user_create_response.user_id),
        request_type="POST",
        stringify_payload=True
    ))

print("[INFO] Access granted for the user (code = " + grant_user_access_response.user_code + ")")

print("[INFO] Executing step 4: Exchanging tokens")

token_exchange_response = ClientTokenResponse(
    http_request_helper.make_request(
        url=URLConfig.get_client_token_fetch_url(),
        content_type="application/x-www-form-urlencoded",
        stringify_payload=True,
        request_type="POST",
        payload=AggregationConfig.get_exchange_token_request_payload(
            user_code=grant_user_access_response.user_code
        )))

print("[INFO] Tokens are successfully exchanged")

print("[INFO] Executing step 5: Get list of available providers from Aggregation")

http_request_helper = HTTPRequestHelper(token_exchange_response)

bypass_response = http_request_helper.make_request(
    url=URLConfig.get_bypass_filters_url(),
    request_type="PUT"
)

providers_response = ProvidersList(
    http_request_helper.make_request(
        url=URLConfig.get_providers_url(),
        request_type="GET"
    )
)

print("[INFO] List of available providers is successfully fetched from Aggregation")

# If provider is not in providers list, the call below will raise an exception
providers_response.get_provider_data(provider_name)
print("[INFO] Provider " + provider_name + " is among available providers")

print("[INFO] Executing step 6: Create credentials for the user")

user_name = str(uuid.uuid4())
print("[INFO] user name to be used = " + user_name)

credentials_create_request_payload = {
    "fields": {
        "username": user_name
    },
    "providerName": provider_name
}

create_credentials_response = CreateCredentialsResponse(
    http_request_helper.make_request(
        url=URLConfig.get_create_credentials_url(),
        content_type="application/json",
        request_type="POST",
        payload=credentials_create_request_payload
    ))

print("[INFO] Credentials is created successfully")
print("[INFO] Credentials id = " + create_credentials_response.credentials_id)

print("[INFO] Executing step 7: Trying to fetch the web page URL for authentication")
attempt_counter = 1
while True:
    print("[INFO] Attempt " + str(attempt_counter) + ": trying to fetch authentication URL")

    credentials_status_response = http_request_helper.make_request(
        url=URLConfig.get_credentials_status_url(create_credentials_response.credentials_id),
        request_type="GET"
    )

    if "supplementalInformation" not in credentials_status_response.keys():
        time.sleep(3)
        attempt_counter += 1
        continue

    authentication_url = json.loads(credentials_status_response["supplementalInformation"])["desktop"]["url"]
    break

print("[INFO] Successfully fetched the URL for authentication")
print("URL to be used for authentication is " + authentication_url)
input("Please open this URL in the browser and after authentication is completed, press ENTER")

print("[INFO] Executing step 8: Trigger the refresh operation for the user")
attempt_counter = 1

while True:

    try:
        print("[INFO] Attempt " + str(attempt_counter) + ": trying to execute refresh for the user")

        refresh_credentials_response = http_request_helper.make_request(
            url=URLConfig.get_credentials_refresh_url(create_credentials_response.credentials_id),
            content_type="application/json",
            request_type="POST"
        )

        break

    except Exception as err:
        pass

    time.sleep(3)
    attempt_counter += 1

print("[INFO] Refresh is completed")
print("[INFO] Executing step 9: Fetching list of accounts from Aggregation")
print("To test the refresh is successful, you need to check the list of accounts and see if the accounts that"
      "the agent is supposed to fetch are there. Sometimes updating the list takes time so if you do not see the"
      "accounts in the list, wait a bit and execute the step again by pressing ENTER")

while True:

    accounts_list_response = http_request_helper.make_request(
        request_type="GET",
        url=URLConfig.get_accounts_list_url()
    )

    accounts_list_response = accounts_list_response["accounts"]

    """
    Filter the accounts so that only the accounts related with the user and credential that we created
    will remain in the list
    """

    accounts_list_response = list(filter(lambda account:
                                    account["credentialsId"] == create_credentials_response.credentials_id
                                    and
                                    account["userId"] == create_credentials_response.user_id, accounts_list_response))

    print("[INFO] List of accounts are successfully fetched")
    print("[INFO] Credentials id = " + create_credentials_response.credentials_id)
    print("[INFO] User id = " + create_credentials_response.user_id)
    print(json.dumps(accounts_list_response, indent=1))
    i = input("To try again, press ENTER, to continue press Q")
    if i.lower() == "q":
        break

print("[INFO] Executing step 10: Fetching list of transactions from Aggregation")
print("To test the refresh is successful, you need to check the list of transactions and see if the transactions that"
      "the agent is supposed to fetch are there. Sometimes updating the list takes time so if you do not see the"
      "transactions in the list, wait a bit and execute the step again by pressing ENTER")

while True:

    transactions_list_response = http_request_helper.make_request(
        request_type="GET",
        url=URLConfig.get_transactions_list_url()
    )

    """
    Filter the transactions so that only the transactions related with the user and credential that we created
    will remain in the list
    """

    transactions_list_response = list(filter(lambda transaction:
                                    transaction["userId"] == create_credentials_response.user_id, transactions_list_response))

    print("[INFO] List of transactions are successfully fetched")
    print("[INFO] Credentials id = " + create_credentials_response.credentials_id)
    print("[INFO] User id = " + create_credentials_response.user_id)
    print(json.dumps(transactions_list_response, indent=1))
    i = input("To try again, press ENTER, to quit press Q")
    if i.lower() == "q":
        break
