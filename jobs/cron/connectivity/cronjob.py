#!/usr/bin/env python3

import sys
import requests

response = None

try:
    print("Connecting to aggregation...")
    sys.stdout.flush()

    response = requests.get("https://aggregation/monitoring/connectivity", verify = False)
except requests.exceptions.ConnectionError as error:
    print("Failed to connect to Aggregation Service: {}".format(error))
    sys.exit(1)

if(response.status_code is 200):
    print(response.text)
    sys.exit(0)
else:
    print("Some other error happened : {}".format(response.status_code))
    sys.exit(1)
