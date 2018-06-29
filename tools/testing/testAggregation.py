from time import sleep
import time
import sys
import os
import getopt
import json
import requests

SERVER_HOST = 'http://127.0.0.1:5000'

### START ENDPOINTS ###
CREATE_CREDENTIAL = SERVER_HOST + '/credentials/create'
REFRESH_CREDENTIAL = SERVER_HOST + '/credentials/refresh/{}'
STATUS_CREDENTIAL = SERVER_HOST + '/credentials/status/{}'
LIST_CREDENTIALS = SERVER_HOST + '/credentials/list'
SUPPLEMENTAL = SERVER_HOST + '/credentials/supplemental'
GET_PROVIDER = SERVER_HOST + '/providers/{}'
LIST_PROVIDERS = SERVER_HOST + '/providers/list'
LIST_PROVIDERS_BY_MARKET = LIST_PROVIDERS + '/{}'
PING = SERVER_HOST + '/ping'

ERROR_STATUSES = ['AUTHENTICATION_ERROR', 'TEMPORARY_ERROR', 'PERMANENT_ERROR', 'NOT_IMPLEMENTED_ERROR', 'UNCHANGED']

EXIT_OPERATION = ['exit', 'e']
GET_OPERATION = ['get', 'g']
LIST_OPERATION = ['list', 'l']
REFRESH_OPERATION = ['refresh', 'r']
CREATE_OPERATION = ['create', 'c']
ACCOUNT_OPERATION = ['account', 'a']
CREDENTIALS_OPERATION = ['credentials', 'c']
PROVIDERS_OPERATION = ['providers', 'p']
SUPPLEMENTAL_OPERATION = ['supp', 's']
MARKET_OPERATION = ['market', 'm']

### END ENDPOINTS ###

### START CLIENT METHODS ###

def create_credential():
    username = raw_input('Username: ')
    providerName = raw_input('Provider name: ')
    credentialsType = raw_input('Credentials type (MOBILE_BANKID, PASSWORD): ')

    createCredentialsRequest = {
        'username': username,
        'providerName': providerName,
        'credentialsType': credentialsType
    }

    responseObject = requests.post(CREATE_CREDENTIAL, json.dumps(createCredentialsRequest), headers={'Content-Type': 'application/json'})
    return responseObject.text

def refresh_credential():
    credentialsId = raw_input('Credentials id: ')
    statusBeforeUpdate = json.loads(credentials_status(credentialsId))

    startTime = get_time_in_millis()
    responseObject = requests.post(str.format(REFRESH_CREDENTIAL, credentialsId))

    output = {
        'message': str.format('Refreshing credential with id {}', credentialsId)
    }

    while True:
        if (get_time_in_millis() - startTime) / 1000.0 >= 120:
            return '\nRefresh timed out.'

        currentStatus = json.loads(credentials_status(credentialsId))
        if not currentStatus:
            return '\nRefresh failed.'
        
        if currentStatus['message']:
            return str.format('\nRefresh failed with message: {}', currentStatus['message'])

        if statusBeforeUpdate['timestamp'] == currentStatus['timestamp']:
            print output
            sleep(1)
            continue
        
        # Update the status
        statusBeforeUpdate = currentStatus
        
        status = currentStatus['status']
        if status == 'AWAITING_SUMMPLEMENTAL_INFORMATION':
            suppResponse = supplemental_information(credentialsId)
            if not suppResponse:
                return '\nSupplemental information request failed.'
            elif not suppResponse == 204:
                return '\nSupplemental information request failed.'
        
        if status == 'AWAITING_MOBILE_BANKID_AUTHENTICATION':
            output['message'] = 'Awaiting mobile bankid authentication'
            continue

        if status == 'AWAITING_THIRD_PARTY_APP_AUTHENTICATION':
            output['message'] = 'Awaiting third party app authentication'
            continue

        if status in ERROR_STATUSES:
            return str.format('Refresh failed with status: {}', status)

        if currentStatus['status'] == 'UPDATED':
            return '\nRefresh completed.'

def list_credentials():
    return requests.get(LIST_CREDENTIALS).text

def list_accounts():
    cid = raw_input('Credentials id: ')
    return requests.get(SERVER_HOST + str.format('/credentials/{}/accounts', cid)).text

def credentials_status(cid=None):
    if not cid:
        cid = raw_input('Credentials id: ')
    
    return requests.get(str.format(STATUS_CREDENTIAL, cid)).text

def supplemental_information(cid=None):
    if not cid:
        cid = raw_input('Credentials id: ')
    
    supplemental = raw_input('Aggregation is requesting supplemental information: ')
    supplementalRequest = {
        'credentialsId': cid,
        'supplementalInformation': supplemental
    }

    responseObject = requests.post(SUPPLEMENTAL, json.dumps(supplementalRequest), headers={'Content-Type': 'application/json'})
    return responseObject

def list_providers():
    responseObject = requests.get(LIST_PROVIDERS).text
    return clean_providers(json.loads(responseObject))

def list_providers_by_market():
    market = raw_input('Market: ')
    responseObject = requests.get(str.format(LIST_PROVIDERS_BY_MARKET, market)).text
    return clean_providers(json.loads(responseObject))

def get_provider():
    providerName = raw_input('Provider name: ')
    responseObject = requests.get(str.format(GET_PROVIDER, providerName)).text
    return clean_providers([json.loads(responseObject)])

### END CLIENT METHODS ###

### START HELPER METHODS ###

def showHelp(f, argv):
    h = "%s [-h] [-a aggregationHost]\n" % argv[0]
    h += "  -h/--help     	  This menu\n"
    h += "  -s/--server-host=  Server host (including port)\n"
    print >>f, h

def get_time_in_millis():
	return int(round(time.time() * 1000))

def prettify(toPrettify):
	return json.dumps(toPrettify, sort_keys=True, indent=4, separators=(',', ': '))

def clean_providers(listOfProviders):
    if not listOfProviders:
        return prettify([])

    clean_provider = lambda provider: {'status': provider['status'], 'displayName': provider['displayName'], 'name': provider['name'], 'capabilities': provider['capabilities'], 'passwordHelpText': provider['passwordHelpText'], 'currency': provider['currency'], 'fields': provider['fields'], 'type': provider['type'], 'market': provider['market']}

    return prettify(map(clean_provider, listOfProviders))

def credentials():
    while True:
        print '''
 -- Credentials operations ----------
|                                    |
| Create: create / c                 |
| Refresh: refresh / r               |
| Get one: get / g                   |
| List all: list / l                 |
| Supplemental information: supp / s |
| List accounts: accounts / a        |
|                                    |
| Exit: exit / e                     |
|                                    |
 ------------------------------------
'''

        userInput = raw_input('Operation: ')
            
        if userInput in EXIT_OPERATION:
            break
        elif userInput in CREATE_OPERATION:
            print 'Response: \n' + create_credential()
        elif userInput in REFRESH_OPERATION:
            print refresh_credential()
        elif userInput in SUPPLEMENTAL_OPERATION:
            print supplemental_information()
        elif userInput in GET_OPERATION:
            print credentials_status()
        elif userInput in LIST_OPERATION:
            print list_credentials()
        elif userInput in ACCOUNT_OPERATION:
            print list_accounts()
        

def providers():
    while True:
        print '''
 -- Providers operations ------
|                              |
| List all: list / l           |
| List by market: market / m   |
| Get one: get / g             |
|                              |
| Exit: exit / e               |
|                              |
 ------------------------------
'''

        userInput = raw_input('Operation: ')

        if userInput in EXIT_OPERATION:
            break
        elif userInput in MARKET_OPERATION:
            print list_providers_by_market()
        elif userInput in GET_OPERATION:
            print get_provider()
        elif userInput in LIST_OPERATION:
            print list_providers()

### END HELPER METHODS ###

def main(argv):
    try:
        opts, args = getopt.getopt(
                                argv[1:],
                                "hs:",
                                ["help", "server-host="]
                            )
    except getopt.GetoptError:
        showHelp(sys.stderr, argv)
        return 1
    
    global SERVER_HOST
    for opt, arg in opts:
        if opt in ("-s", "--server-host"):
            SERVER_HOST = arg
        elif opt in ("-h", "--help"):
            showHelp(sys.stdout, argv)
            return 0
    
    try:
        response = requests.get(PING)
    except:
        print str.format("\n ERROR: Could not find a running webServer on {} \n", SERVER_HOST) 
        return 1
    
    while True:
        print '''
 -- Operations ----------------
|                              |
| Credentials: credentials / c |
| Providers: providers / p     |
|                              |
| Exit application: exit / e   |
|                              |
 ------------------------------
'''

        userInput = raw_input('Operation: ')
        
        if userInput in EXIT_OPERATION:
            break
        elif userInput in CREDENTIALS_OPERATION:
            credentials()
        elif userInput in PROVIDERS_OPERATION:
            providers()
    
    print '''
 -------------------------------------------------------------------------------------
|                                                                                     |
| Leaving application. Your don\'t forget to shut down the webServer if you are done.  |
|                                                                                     |
 -------------------------------------------------------------------------------------
'''
    return 0

if __name__ == "__main__":
    sys.exit(main(sys.argv))