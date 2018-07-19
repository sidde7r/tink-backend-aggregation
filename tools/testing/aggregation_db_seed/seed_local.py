#!/usr/bin/python
import json
import pymysql
import yaml
import substring
import sys
import os
import os, sys, select, subprocess
import time


#########################
#Configuration Variables#
#########################
path = "../../../data/seeding/providers-" + sys.argv[1] + ".json"
bazelRelativePath = "../../../"
aggregatonConfigurationsFile = "../../../etc/development-minikube-aggregation-server.yml"

#########################
#Database Insert Values #
#########################
clusterHostConfigurationTable = "cluster_host_configuration"
clusterHostDefaultValues = {
    "clusterid" :"local-development",
    "host":"http://127.0.0.1:5000",
    "apitoken": "devtoken",
    "base64encodedclientcertificate":"",
    "disablerequestcompression": False}

clusterCryptoConfigurationTable = "cluster_crypto_configurations"
clusterCryptoConfigurationDefaultValues = {
    "clusterid":"local-development",
    "keyid": "1",
    "base64encodedkey":"'QUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUE='"}

deleteQuery = "delete from cluster_provider_configurations where (clusterid, providername) in (select 'local-development', name from provider_configurations); "
joinQuery = "insert into cluster_provider_configurations (clusterid, providername) select 'local-development', name from provider_configurations;"

host = ""
password = ""
username = ""
market = ""
currency = ""


def join(columns, rowdict):
    for row in rowdict:
        row = row.lower()

    rowdict = {k.lower(): v for k, v in rowdict.items()}
    returnDict = {}
    for column in columns:
        if column['Field'].lower() in rowdict:
            returnDict[column['Field']] = rowdict[column['Field']]

    return returnDict

def findPrimaryKey(definedColumns):
    for a in definedColumns:
        if a['Key']:
            return a['Field']

def mysql_insert(conn, table, row):
    cols = row.keys()
    vals = row.values()
    sql = "INSERT INTO {} ({}) VALUES ({})".format(
        table,
        ', '.join(cols),
        ', '.join(['%s'] * len(cols)));
    conn.cursor().execute(sql, vals)
    conn.commit()

def rowExist(conn, selectWhat, table, primaryKey, primaryValue):
    sql = "select %s from (%s) where (%s) = %s"
    print "Looking for rows with the column set to: " + primaryValue
    cursor = conn.cursor()
    primaryValue = "'" + primaryValue + "'"
    cursor.execute(sql % (selectWhat, table,primaryKey, primaryValue))
    val = cursor.fetchall()
    conn.commit()
    return not val

def insertLocalDevelopmentCrypto(conn):
    if rowExist(conn, "clusterid", clusterCryptoConfigurationTable, "clusterid", clusterCryptoConfigurationDefaultValues['clusterid']):
        mysql_insert(conn, clusterCryptoConfigurationTable, clusterCryptoConfigurationDefaultValues)
    else:
        print "cluster_crypto_configurations already up to date"

def insertIntoClusterHostConfiguration(conn):
    if rowExist(conn, "clusterid", clusterHostConfigurationTable, "clusterid", clusterHostDefaultValues['clusterid']):
        mysql_insert(conn, clusterHostConfigurationTable, clusterHostDefaultValues)
    else:
        print "cluster_configurations already up to date"
    sqlExecutor(conn, deleteQuery)
    sqlExecutor(conn, joinQuery)


def sqlExecutor(conn, sql):
    cursor = conn.cursor()
    cursor.execute(sql)
    val = cursor.fetchall()
    conn.commit()
    return val


def getConnection():
    with open(aggregatonConfigurationsFile, 'r') as stream:
        try:
            aggregatonConfigurations = (yaml.load(stream))
            dbconnectorList = aggregatonConfigurations['database']['url'].split("/")
            for fragment in dbconnectorList:
                if len(fragment.split(".")) == 4:
                    host = fragment

            username = aggregatonConfigurations['database']['username']
            password = aggregatonConfigurations['database']['password']
            db = pymysql.connect(host=host.split(":")[0],
                                port=int(host.split(":")[1]),    # your host, usually localhost
                                db='tink',
                                user=username,
                                passwd=password,
                                cursorclass=pymysql.cursors.DictCursor)
            return db
        except yaml.YAMLError as exc:
            print(exc)

def seedDatabase(os):
    os.chdir(bazelRelativePath)
    serverArgs = ['bazel run :aggregation seed-providers-for-market --jvmopt="-Dmarket=' + sys.argv[1].upper() + '" etc/development-minikube-aggregation-server.yml']
    server = subprocess.Popen(serverArgs, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    
    while True:
        serverNextLine = server.stdout.readline()
        if serverNextLine != '' :
            if  '[' in serverNextLine and ']' in serverNextLine and 'INFO' not in serverNextLine: 
                print "Building bazel seeder. \n Progress: " + serverNextLine.split(']')[0].split('[')[1]
            elif 'INFO' in serverNextLine:
                print "Seeding"
            else:
                sys.stdout.write(serverNextLine)
                sys.stdout.flush
        if server.poll() != None:
            break


filePath = os.path.abspath(__file__)
os.chdir(os.path.dirname(filePath))

db = getConnection()

tablename = "provider_configurations"
inputfile = file(path, "r")
jsonString = inputfile.read()
providers = json.loads(jsonString)
market = providers['market']
currency = providers['currency']

seedDatabase(os)

insertLocalDevelopmentCrypto(db)
insertIntoClusterHostConfiguration(db)