#!/usr/bin/env python3

import sys
import requests

response = None

try:
    response = requests.get('https://192.168.99.100:32023/initiate', verify=False)
except requests.exceptions.ConnectionError as error:
    print("Failed to connect to NASA: " + format(error))
    sys.exit(1)

if(response.status_code is 200):
    print(response.text)
    sys.exit(0)
else:
    print("Some other error happened : " + str(response.status_code))
    sys.exit(1)
