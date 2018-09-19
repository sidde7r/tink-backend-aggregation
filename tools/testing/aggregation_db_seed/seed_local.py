#!/usr/bin/python
import json
import pymysql
import yaml
import substring
import os, sys, select, subprocess
import getopt
import time


#########################
#Configuration Variables#
#########################
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
    "disablerequestcompression": False,
    "aggregatoridentifier": "Tink (+https://www.tink.se/; noc@tink.se)"}

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

def mysql_insert(db, tableName, dataDict):
    cols = dataDict.keys()
    vals = dataDict.values()
    sql = "INSERT INTO {} ({}) VALUES ({})".format(
        tableName,
        ', '.join(cols),
        ', '.join(['%s'] * len(cols)))
    db.cursor().execute(sql, vals)
    db.commit()

def row_exist(db, selectWhat, tableName, primaryKey, primaryValue):
    sql = "SELECT %s FROM {} WHERE {} = %s".format(tableName, primaryKey)
    print "Looking for rows with the column set to: " + primaryValue
    cursor = db.cursor()
    cursor.execute(sql, (selectWhat, primaryValue))
    val = cursor.fetchone()
    db.commit()
    return val is not None

def insert_local_development_crypto(db):
    if row_exist(db, "clusterid", clusterCryptoConfigurationTable, "clusterid", clusterCryptoConfigurationDefaultValues['clusterid']):
        print "cluster_crypto_configurations already up to date"
    else:
        mysql_insert(db, clusterCryptoConfigurationTable, clusterCryptoConfigurationDefaultValues)

def insert_into_cluster_host_configuration(db):
    if row_exist(db, "clusterid", clusterHostConfigurationTable, "clusterid", clusterHostDefaultValues['clusterid']):
        print "cluster_configurations already up to date"
    else:
        mysql_insert(db, clusterHostConfigurationTable, clusterHostDefaultValues)
    sql_executor(db, deleteQuery)
    sql_executor(db, joinQuery)


def sql_executor(db, sql):
    cursor = db.cursor()
    cursor.execute(sql)
    val = cursor.fetchall()
    db.commit()
    return val


def get_connection():
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

def seed_database(os, seedMarket):
    os.chdir(bazelRelativePath)
    serverArgs = ['bazel run :aggregation seed-providers-for-market --jvmopt="-Dmarket=' + seedMarket.upper() + '" etc/development-minikube-aggregation-server.yml']
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

def showHelp(f, argv):
    h = "%s [-h] [-a] [-c Aggregation controller host] [-f] [-m Market]\n" % argv[0]
    h += "  -h/--help     	  This menu\n"
    h += "  -a/--aggregation     	  Setup to only test Aggregation\n"
    h += "  -c/--custom-host=       Server host for Aggregation controller (including port)\n"
    h += "  -f/--full     	  Setup aggregation to use Aggregation controller\n"
    h += "  -m/--market     	  Market to seed (Like SE, DK, ...)\n"
    print >>f, h

def main(argv):
    try:
        opts, args = getopt.getopt(
                                argv[1:],
                                "hfc:am:",
                                ["help", "full", "custom=", "aggregation", "market="]
                            )
    except getopt.GetoptError:
        showHelp(sys.stderr, argv)
        return 1

    global clusterHostDefaultValues
    seedMarket = ""
    isSeedProviders = False
    for opt, arg in opts:
        if opt in ("-f", "--full"):
            clusterHostDefaultValues['host'] = 'http://127.0.0.1:9098'
        elif opt in ("-a", "--aggregation"):
            clusterHostDefaultValues['host'] = 'http://127.0.0.1:5000'
        elif opt in ("-m", "--market"):
            isSeedProviders = True
            seedMarket = arg
        elif opt in ("-c", "--custom-host"):
            clusterHostDefaultValues['host'] = arg
        elif opt in ("-h", "--help"):
            showHelp(sys.stdout, argv)
            return 0

    filePath = os.path.abspath(__file__)
    os.chdir(os.path.dirname(filePath))

    db = get_connection()

    tablename = "provider_configurations"

    if isSeedProviders:
        path = "../../../data/seeding/providers-" + seedMarket + ".json"
        inputfile = file(path, "r")
        jsonString = inputfile.read()
        providers = json.loads(jsonString)
        market = providers['market']
        currency = providers['currency']
        seed_database(os, seedMarket)

    insert_local_development_crypto(db)
    insert_into_cluster_host_configuration(db)
    return 0

if __name__ == "__main__":
    sys.exit(main(sys.argv))