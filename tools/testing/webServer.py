# coding=utf-8
from tinydb import TinyDB, Query, where
from flask import Flask, request, abort, Response, jsonify, make_response
from functools import wraps
import gzip
import StringIO
import requests
import ast
import sys
import getopt
import json
import uuid
import time

from logging.config import dictConfig

dictConfig({
    'version': 1,
    'formatters': {'default': {
        'format': '[%(asctime)s] %(levelname)s: %(message)s',
    }},
    'handlers': {'wsgi': {
        'class': 'logging.StreamHandler',
        'stream': 'ext://flask.logging.wsgi_errors_stream',
        'formatter': 'default'
    }},
    'root': {
        'level': 'INFO',
        'handlers': ['wsgi']
    }
})

app = Flask(__name__)

### START - CONSTANTS ###

AGGREGATION_HOST = 'http://127.0.0.1:9095'

DATA_BASE = None
CREDENTIALS_TABLE = None
ACCOUNTS_TABLE = None

LOG = None

CLUSTER_ENVIRONMENT_KEY = 'x-tink-cluster-environment'
CLUSTER_ENVIRONMENT_VALUE = 'development'
CLUSTER_NAME_KEY = 'x-tink-cluster-name'
CLUSTER_NAME_VALUE = 'local'

CONTENT_TYPE_KEY = 'content-type'
ACCEPT_KEY = 'accept'
APPLICATION_JSON = 'application/json'

GET_HEADERS = {CLUSTER_ENVIRONMENT_KEY: CLUSTER_ENVIRONMENT_VALUE , CLUSTER_NAME_KEY: CLUSTER_NAME_VALUE, ACCEPT_KEY: APPLICATION_JSON}
POST_HEADERS = {CLUSTER_ENVIRONMENT_KEY: CLUSTER_ENVIRONMENT_VALUE , CLUSTER_NAME_KEY: CLUSTER_NAME_VALUE, CONTENT_TYPE_KEY: APPLICATION_JSON, ACCEPT_KEY: APPLICATION_JSON}

### END - CONSTANTS ###

### START - DECORATORS ###

def validate_request(*keys):
	def decorator(func):
		@wraps(func)
		def decorated_function(*args, **kwargs):
			if not request.json:
				abort(400, 'Your request is not application/json')
			for key in keys:
				if key not in request.json:
					abort(400, str.format('Your request is missing {}', key))
				if not len(request.json[key]):
					abort(400, str.format('The field \'{}\' did not contain a value', key))
				return func(*args, **kwargs)
		return decorated_function
	return decorator

@app.errorhandler(400)
def custom400(error):
	return make_response(jsonify({'message': error.description}), 400)

### END - DECORATORS ###

### START - USABLE ENDPOINTS ###

@app.route("/credentials/create", methods = ['POST'])
@validate_request('username', 'providerName', 'credentialsType')
def create_credentials():
	credentialsRequest = create_credentials_request()
	r = requests.post(AGGREGATION_HOST + '/aggregation/create', data=json.dumps(credentialsRequest), headers=POST_HEADERS)
	credential = json.loads(r.text)
	credential['timestamp'] = get_time_in_millis()
	CREDENTIALS_TABLE.insert(credential)
	return prettify_dict({'credentialsId': credential['id']})

@app.route("/credentials/refresh/<id>", methods = ['POST'])
def refresh_credentials(id):
	credentialsRequest = create_credentials_request(id)
	credentialsRequest['manual'] = True
	r = requests.post(AGGREGATION_HOST + '/aggregation/refresh', data=json.dumps(credentialsRequest), headers=POST_HEADERS)
	return ('', 204)

@app.route("/credentials/supplemental", methods = ['POST'])
@validate_request('credentialsId', 'supplementalInformation')
def credentials_supplemental():
	supplementalRequest = request.json
	credentials = CREDENTIALS_TABLE.search(Query().id == supplementalRequest['credentialsId'])
	if not credentials:
		abort(400, 'Not a valid credentials id.')

	credential = credentials[0]
	if not credential['status'] == 'AWAITING_SUPPLEMENTAL_INFORMATION':
		abort(400, 'This credentials is not awaiting supplemental information.')

	r = requests.post(AGGREGATION_HOST + '/aggregation/supplemental', data=json.dumps(supplementalRequest), headers=POST_HEADERS)
	CREDENTIALS_TABLE.update({'status': 'UPDATING', 'timestamp': get_time_in_millis()}, where('id') == credential['id'])
	return ('', 204)

@app.route("/credentials/list", methods = ['GET'])
def list_credentials():
	credentials = CREDENTIALS_TABLE.search(Query().id.matches('.*'))
	filter_credentials_info = lambda credential: {'id': credential['id'], 'status': credential['status'], 'type': credential['type'], 'username': credential['username']}

	responseList = map(filter_credentials_info, credentials)
	return (prettify_dict(responseList), 200)

@app.route("/providers/list/<market>", methods = ['GET'])
def list_provider_by_market(market):
	print market
	return requests.get(AGGREGATION_HOST + '/providers/' + market + '/list', headers=GET_HEADERS).text

@app.route("/providers/<providername>", methods = ['GET'])
def list_provider(providername):
	if not providername:
		return list_providers()

	return json.dumps(get_provider(providername))


@app.route("/providers/list", methods = ['GET'])
def list_providers(*args):
	return requests.get(AGGREGATION_HOST + '/providers/list', headers=GET_HEADERS).text

@app.route("/credentials/status/<id>", methods = ['GET'])
def get_credential(id):
	credentials = CREDENTIALS_TABLE.search(Query().id == id)
	if not credentials:
		return (json.dumps({}, 204))

	response = {
		'status': credentials[0]['status'],
		'timestamp': credentials[0]['timestamp']
	}

	return (prettify_dict(response), 200)

@app.route("/ping", methods = ['GET'])
def ping():
	return "pong"

### END - USABLE ENDPOINTS ###

### START - ENDPOINTS FOR AGGREGATION SERVICE ###

@app.route("/aggregation/controller/v1/system/update/credentials/update", methods = ['POST'])
def update_credentials_status():
	responseObject = get_json(request)
	credentials = responseObject['credentials']
	CREDENTIALS_TABLE.update({'status': credentials['status'], 'timestamp': get_time_in_millis()}, where('id') == credentials['id'])
	return Response({}, status=200, mimetype="application/json")

@app.route("/aggregation/controller/v1/system/update/accounts/update", methods = ['POST'])
def update_account():
	responseData = get_json(request)
	aggregationAccount = to_aggregation_account(responseData['account'])
	listOfAccounts = ACCOUNTS_TABLE.search(((where('bankId') == aggregationAccount['bankId']) & (where('credentialsId') == aggregationAccount['credentialsId'])))
	if not listOfAccounts:
		ACCOUNTS_TABLE.insert(aggregationAccount)
	else:
		ACCOUNTS_TABLE.update(aggregationAccount, where('bankId') == aggregationAccount['bankId'])
	LOG.info("Request to update account: \n %s", prettify_dict(aggregationAccount))
	return Response(json.dumps(responseData['account']), status=200, mimetype="application/json")

@app.route("/aggregation/controller/v1/system/update/accounts/process", methods = ['POST'])
def process_accounts():
	responseData = get_json(request)
	LOG.info("Received request to process accounts: \n %s", prettify_dict(responseData))
	return Response({}, status=200, mimetype="application/json")

@app.route("/aggregation/controller/v1/system/process/transactions/update", methods = ['POST'])
def process_transactions():
	responseData = get_json(request)
	return Response({}, status=200, mimetype="application/json")

@app.route("/aggregation/controller/v1/system/update/transfer/process", methods = ['POST'])
def process_transfers():
	responseData = get_json(request)
	LOG.info("Request to process transfers: \n %s", prettify_dict(responseData))
	return Response({}, status=200, mimetype="application/json")

@app.route("/aggregation/controller/v1/system/update/accounts/transfer-destinations/update", methods = ['POST'])
def process_transfer_destinations():
	responseData = get_json(request)
	return Response({}, status=200, mimetype="application/json")

@app.route("/aggregation/controller/v1/credentials/sensitive", methods = ['PUT'])
def credentials_sensitive():
	responseData = get_json(request)
	CREDENTIALS_TABLE.update({'sensitiveDataSerialized': responseData['sensitiveData']}, where('id') == responseData['credentialsId'])
	return Response({}, status=200, mimetype="application/json")
	
### START - ENDPOINTS FOR AGGREGATION SERVICE ###

### START - HELPER METHODS ###

def get_provider(providerName):
	if not providerName:
		abort(400, 'Provider name is missing.')
	r = requests.get(AGGREGATION_HOST + '/providers/' + providerName, headers=GET_HEADERS)
	if not r.text:
		abort(400, 'Invalid provider name')
	return json.loads(r.text)

def create_credentials_request(credentialsId=None):
	credentials = None
	provider = None
	user = None

	if credentialsId:
		listOfCredentials = CREDENTIALS_TABLE.search(Query().id == credentialsId)
		if not listOfCredentials:
			abort(400, 'Invalid credentials id.')
		if len(listOfCredentials) != 1:
			abort(400, 'There exists more than one credentials with this id. This should not happen.')
		credentials = listOfCredentials[0]
		user = create_user(credentials['userId'])
		provider = get_provider(credentials['providerName'])
	
	if not credentials:
		user = create_user()
		provider = get_provider(request.json['providerName'])
		credentials = create_credential(user['id'])

	if not provider:
		abort(400, 'Invalid provider name')

	return {
		'user': user,
		'credentials': credentials,
		'provider': provider,
		'accounts': []
	}

def create_credential(userId):
	return {
		'id': random_uuid_string(),
		'username': request.json['username'],
		'providerName': request.json['providerName'],
		'userId': userId,
		'type': request.json['credentialsType'],
		'status': 'CREATED'
	}

def to_aggregation_account(coreAccount):
	return {
		'id': coreAccount['id'],
		'accountNumber': coreAccount['accountNumber'],
		'availableCredit': coreAccount['availableCredit'],
		'balance': coreAccount['balance'],
		'bankId': coreAccount['bankId'],
		'credentialsId': coreAccount['credentialsId'],
		'holderName': coreAccount['holderName'],
		'identifiers': coreAccount['identifiers'],
		'name': coreAccount['name'],
		'transferDestinations': coreAccount['transferDestinations'],
		'type': coreAccount['type'],
		'userId': coreAccount['userId']
	}

def create_user(id=None):
	if not id:
		id = random_uuid_string()
	
	return {
		'id': id,
		'profile': {"locale": "sv_SE"}
	}

def random_uuid_string():
	return str(uuid.uuid4()).replace('-', '')

def prettify_dict(dictToPrettify):
	return json.dumps(dictToPrettify, sort_keys=True, indent=4, separators=(',', ': '))

def get_json(requestObject):
	'''
	`request.data` is a compressed string and `gzip.GzipFile` doesn't work on strings.
	We use StringIO to make it look like a file with this:
	'''
	fakefile = StringIO.StringIO(requestObject.data)
	'''
	Now we can load the compressed 'file' into the `uncompressed` variable.
	While we're at it, we tell gzip.GzipFile to use the 'rb' mode
	'''
	uncompressed = gzip.GzipFile(fileobj=fakefile, mode='rb')
	'''
	Since StringIOs aren't real files, you don't have to close the file.
	This means that it's safe to return its contents directly:
	'''
	return json.loads(uncompressed.read())

def showHelp(f, argv):
    h = "%s [-h] [-a aggregationHost]\n" % argv[0]
    h += "  -h/--help     	  This menu\n"
    h += "  -a/--aggregation-host=  Aggregation host (including port)\n"
    print >>f, h

def get_time_in_millis():
	return int(round(time.time() * 1000))

### END - HELPER METHODS ###

def main(argv):
	try:
		opts, args = getopt.getopt(
                                argv[1:],
                                "ha:",
                                ["help", "aggregation-host="]
                            )
	except getopt.GetoptError:
		print "HERE"
		showHelp(sys.stderr, argv)
		return sys.exit(1)

	global AGGREGATION_HOST
	for opt, arg in opts:
		if opt in ("-a", "--aggregation-host"):
			AGGREGATION_HOST = arg
		elif opt in ("-h", "--help"):
			showHelp(sys.stdout, argv)
			sys.exit(0)

	global DATA_BASE, CREDENTIALS_TABLE, ACCOUNTS_TABLE, LOG
	if not DATA_BASE:
		DATA_BASE = TinyDB('db.json')
	if not CREDENTIALS_TABLE:
		CREDENTIALS_TABLE = DATA_BASE.table('credentials')
	if not ACCOUNTS_TABLE:
		ACCOUNTS_TABLE = DATA_BASE.table('accounts')
	LOG = app.logger

	print str.format("\n Starting testing service with aggregation host: {} \n", AGGREGATION_HOST)

if __name__ == "__main__":
    app.run(main(sys.argv))