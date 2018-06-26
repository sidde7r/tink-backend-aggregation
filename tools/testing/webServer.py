# coding=utf-8
from tinydb import TinyDB, Query, where
from flask import Flask, request, abort, Response
from functools import wraps
import gzip
import StringIO
import requests
import ast
import sys
import json
import uuid

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
USER_TABLE = None
ACCOUNTS_TABLE = None
TRANSACTION_TABLE = None
TRANSFER_TRABLE = None

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
				return abort(400)
			for key in keys:
				if key not in request.json:
					return abort(400)
				if not len(request.json[key]):
					return abort(400)
				return func(*args, **kwargs)
		return decorated_function
	return decorator

### END - DECORATORS ###

### START - ENDPOINTS ###

@app.route("/credentials/create", methods = ['POST'])
@validate_request('username', 'providerName', 'credentialsType')
def create_credentials():
	credentialsRequest = create_credentials_request()
	r = requests.post(AGGREGATION_HOST + '/aggregation/create', data=json.dumps(credentialsRequest), headers=POST_HEADERS)
	credential = json.loads(r.text)
	if CREDENTIALS_TABLE.search(Query().id == credential['id']):
		abort(400)
	CREDENTIALS_TABLE.insert(credential)
	return json.dumps({'credentialsId': credential['id']})

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
	r = requests.post(AGGREGATION_HOST + '/aggregation/supplemental', data=json.dumps(supplementalRequest), headers=POST_HEADERS)
	return ('', 204)

@app.route("/providers/list", methods = ['GET', 'POST'])
def list_providers(*args):
	if request.method == 'POST':
		if not request.json:
			abort(400)
		return get_provider(request.json['providerName'])
	if not len(request.args):
		return requests.get(AGGREGATION_HOST + '/providers/list', headers=GET_HEADERS).text
	if request.args.get('market'):
		return requests.get(AGGREGATION_HOST + '/providers/' + request.args.get('market') + '/list', headers=GET_HEADERS).text
	abort(400)

@app.route("/aggregation/controller/v1/system/update/credentials/update", methods = ['POST'])
def update_credentials_status():
	responseObject = get_json(request)
	credentials = responseObject['credentials']
	CREDENTIALS_TABLE.update({'status': credentials['status']}, where('id') == credentials['id'])
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
	requestData = get_json(request)
	CREDENTIALS_TABLE.update({'sensitiveDataSerialized': requestData['sensitiveData']}, where('id') == requestData['credentialsId'])
	return Response({}, status=200, mimetype="application/json")
	
### END - ENDPOINTS ###

### START - HELPER METHODS ###

def get_provider(providerName):
	if not providerName:
		abort(400)
	r = requests.get(AGGREGATION_HOST + '/providers/' + providerName, headers=GET_HEADERS)
	if not r.text:
		abort(400)
	return json.loads(r.text)

def create_credentials_request(credentialsId=None):
	credentials = None
	provider = None
	user = None

	if credentialsId:
		listOfCredentials = CREDENTIALS_TABLE.search(Query().id == credentialsId)
		if not listOfCredentials:
			abort(400)
		if len(listOfCredentials) != 1:
			abort(400)
		credentials = listOfCredentials[0]
		user = create_user(credentials['userId'])
		provider = get_provider(credentials['providerName'])
	
	if not credentials:
		user = create_user()
		provider = get_provider(request.json['providerName'])
		credentials = create_credential(user['id'])

	if not provider:
		abort(400)

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

### END - HELPER METHODS ###

def main():
	global DATA_BASE, CREDENTIALS_TABLE, ACCOUNTS_TABLE, LOG
	if not DATA_BASE:
		DATA_BASE = TinyDB('db.json')
	if not CREDENTIALS_TABLE:
		CREDENTIALS_TABLE = DATA_BASE.table('credentials')
	if not ACCOUNTS_TABLE:
		ACCOUNTS_TABLE = DATA_BASE.table('accounts')
	LOG = app.logger

if __name__ == "__main__":
    app.run(main())