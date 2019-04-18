#!/usr/bin/env python3

import sys
import requests

response = None

if (len(sys.argv) < 3):
    print("Not enough argument: {}".format(sys.argv))
    sys.exit(1)

cluster_id = sys.argv[1]
environment = sys.argv[2]

request_url = None;
verify = None;

if (cluster_id == "local" and environment == "development"):
    request_url = 'https://192.168.99.100:31011/monitoring/connectivity'
    verify = False
else:
    request_url = "https://aggregation2.{}.{}.tink.se".format(environment, cluster_id)
    verify = False # TODO: put cert here!

try:
    response = requests.get(request_url, verify)
except requests.exceptions.ConnectionError as error:
    print("Failed to connect to Aggregation Service: {}".format(error))
    sys.exit(1)

if(response.status_code is 200):
    print(response.text)
    sys.exit(0)
else:
    print("Some other error happened : {}".format(response.status_code))
    sys.exit(1)
