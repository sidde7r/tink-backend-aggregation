#!/usr/bin/env python2
import random
import hashlib
import requests
import time
import json

CONNECTOR_URL = "http://localhost:9098{}"
N_SIZE = 10000
rand = random.Random()

def generate_transaction(i):
    return {
        "amount": rand.randint(-1000, 3000),
        "date": 1455740874875,
        "description": "ICA",
        "externalId": hashlib.sha256(str(i)).hexdigest(),
        "type": "CREDIT_CARD"
    }

def create_transaction_list(size):
    li = []
    for i in range(size):
        li.append(generate_transaction(i))
    return li

def create_random_account():
    return {
        "balance": 0,
        "externalId": hashlib.sha256(str(random.random())).hexdigest(),
        "name": "PythonGenerated",
        "number": "{:011}".format(rand.randint(0, 1234567890)),
        "type": "CHECKING"
    }

def create_random_user():
    return {
        "externalId": hashlib.sha256(str(random.random())).hexdigest(),
        "token": "secret"
    }

if __name__ == '__main__':
    headers = {"Content-Type": "application/json", "Authorization": "token 0"}

    user = create_random_user()
    account = create_random_account()
    transactions = create_transaction_list(N_SIZE)

    res = requests.post(CONNECTOR_URL.format("/connector/users"), json=user, headers=headers)

    if (res.status_code != 200):
        print res.text
    print "Create user status: {}".format(res.status_code)

    res = requests.post(CONNECTOR_URL.format("/connector/users/{}/accounts".format(user.get("externalId"))),
                        json={"accounts": [account]}, headers=headers)

    if (res.status_code != 200):
        print res.text
    print "Create account status: {}".format(res.status_code)

    transaction_account = {
        "balance": 50,
        "externalId": account.get("externalId"),
        "transactions": transactions
    }

    transaction_request_body = {
        "transactionAccounts": [transaction_account],
        "type": "REAL_TIME"
    }

    start = time.time()
    res = requests.post(CONNECTOR_URL.format("/connector/users/{}/transactions").format(user.get("externalId")),
                  json = transaction_request_body, headers=headers)
    elapsed_time = time.time() - start

    if (res.status_code != 200):
        print res.text
    print "Ingested {} transactions in {:.2f} seconds with response code {}".format(N_SIZE, elapsed_time, res.status_code)

