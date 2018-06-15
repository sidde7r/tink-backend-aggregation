# coding=utf-8
from tinydb import TinyDB, Query
from flask import Flask
from flask import request
from flask import abort
from functools import wraps
import requests
import ast
import sys
import json
import uuid
app = Flask(__name__)

### START - CONSTANTS ###

AGGREGATION_HOST = 'http://127.0.0.1:9095'

DATA_BASE = None
CREDENTIALS_TABLE = None
USER_TABLE = None
ACCOUNT_TABLE = None
TRANSACTION_TABLE = None

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
	CREDENTIALS_TABLE.insert(credential)
	return json.dumps({'credentialsId': credential.get('id')})

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

### END - ENDPOINTS ###

### START - HELPER METHODS ###

def get_provider(providerName):
	if not providerName:
		abort(400)
	r = requests.get(AGGREGATION_HOST + '/providers/' + providerName, headers=GET_HEADERS)
	if not r.text:
		abort(400)
	return json.loads(r.text)

def create_credentials_request():
	provider = get_provider(request.json['providerName'])

	if not provider:
		abort(400)

	user = create_random_user()
	return {
		'user': user,
		'credentials': create_random_credential(user['id']),
		'provider': provider
	}

def create_random_credential(userId):
	return {
		'id': random_uuid_string(),
		'username': request.json['username'],
		'providerName': request.json['providerName'],
		'userId': userId,
		'type': request.json['credentialsType']
	}

def create_random_user():
	return {
		'id': random_uuid_string(),
		'profile': {"locale": "sv_SE"}
	}

def random_uuid_string():
	return str(uuid.uuid4()).replace('-', '')

### END - HELPER METHODS ###

def main():
	global DATA_BASE, CREDENTIALS_TABLE
	if not DATA_BASE:
		DATA_BASE = TinyDB('db.json')
	if not CREDENTIALS_TABLE:
		CREDENTIALS_TABLE = DATA_BASE.table('credentials')

if __name__ == "__main__":
    app.run(main())