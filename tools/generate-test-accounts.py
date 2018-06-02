# Run like `$> python generate-test-accounts.py N` to generate SQL inserts for `N` users, with one credential each, with one account each.
# Customize the properties to fit your needs.
import sys
import uuid


def tinkuuid():
  return str(uuid.uuid4()).replace("-", "")


userTemplate = {
  'id': None,
  'blocked': False,
  'confirmed': False,
  'created': None,
  'endpoint': None,
  'flags': '["ABN_AMRO","TINK_TEST_ACCOUNT"]',
  'hash': None,
  'profile_birth': None,
  'profile_cashbackenabled': False,
  'profile_currency': 'EUR',
  'profile_fraudpersonnumber': None,
  'profile_gender': None,
  'profile_locale': 'nl_NL',
  'profile_market': 'NL',
  'profile_name': None,
  'notificationSettings': '{}',
  'profile_periodadjustedday': 0,
  'profile_periodmode': 'MONTHLY',
  'profile_timezone': 'Europe/Amsterdam',
  'username': None
}

credentialsTemplate = {
  'id': None,
  'debug': False,
  'fields': None,
  'nextupdate': None,
  'payload': None,
  'providername': 'nl-abnamro-abstract',
  'secretkey': None,
  'status': 'UPDATING',
  'statuspayload': None,
  'statusprompt': None,
  'statusupdated': None,
  'supplementalinformation': None,
  'type': 'PASSWORD',
  'updated': None,
  'userid': None
}

accountTemplate = {
  'id': None,
  'accountnumber': None,
  'availablecredit': 0,
  'balance': 0,
  'bankid': None,
  'certaindate': None,
  'credentialsid': None,
  'excluded': False,
  'favored': True,
  'identifiers': None,
  'name': 'Test account',
  'ownership': 1,
  'payload': None,
  'type': 'CHECKING',
  'userid': None,
  'usermodifiedname': False,
  'usermodifiedtype': False
}

accountnumber = 500000000

def _s(s):
  if s is None:
    return 'NULL'
  else:
    return "'{0}'".format(s)

def _b(b):
  if b is True:
    return 'true'
  else:
    return 'false'

def _n(n):
  if n is None:
    return '0'
  else:
    return str(n)

def getFormat(c):
  s = '\n('
  for i in range(0,c):
    s += '{{{0}}}'.format(i)
    if i < (c - 1):
      s += ','
  s += ')'
  return s

def lineEnd(i, length):
  if i == (length - 1):
    return ';'
  else:
    return ','

def getUsersSQL(users):
  sql = 'INSERT INTO `users` (`id`,`blocked`,`confirmed`,`created`,`endpoint`,`flags`,`hash`,`profile_birth`,`profile_cashbackenabled`,`profile_currency`,`profile_fraudpersonnumber`,`profile_gender`,`profile_locale`,`profile_market`,`profile_name`,`notificationSettings`,`profile_periodadjustedday`,`profile_periodmode`,`profile_timezone`,`username`) VALUES '
  f = getFormat(len(userTemplate))
  for i,u in enumerate(users):
    sql += f.format(
      _s(u['id']),
      _b(u['blocked']),
      _b(u['confirmed']),
      _s(u['created']),
      _s(u['endpoint']),
      _s(u['flags']),
      _s(u['hash']),
      _s(u['profile_birth']),
      _b(u['profile_cashbackenabled']),
      _s(u['profile_currency']),
      _s(u['profile_fraudpersonnumber']),
      _s(u['profile_gender']),
      _s(u['profile_locale']),
      _s(u['profile_market']),
      _s(u['profile_name']),
      _s(u['notificationSettings']),
      _n(u['profile_periodadjustedday']),
      _s(u['profile_periodmode']),
      _s(u['profile_timezone']),
      _s(u['username']))
    sql += lineEnd(i, len(users))
  return sql

def getCredentialsSQL(credentials):
  sql = 'INSERT INTO `credentials` (`id`,`debug`,`fields`,`nextupdate`,`payload`,`providername`,`secretkey`,`status`,`statuspayload`,`statusprompt`,`statusupdated`,`supplementalinformation`,`type`,`updated`,`userid`) VALUES '
  f = getFormat(len(credentialsTemplate))
  for i,c in enumerate(credentials):
    sql += f.format(
      _s(c['id']),
      _b(c['debug']),
      _s(c['fields']),
      _s(c['nextupdate']),
      _s(c['payload']),
      _s(c['providername']),
      _s(c['secretkey']),
      _s(c['status']),
      _s(c['statuspayload']),
      _s(c['statusprompt']),
      _s(c['statusupdated']),
      _s(c['supplementalinformation']),
      _s(c['type']),
      _s(c['updated']),
      _s(c['userid']))
    sql += lineEnd(i, len(credentials))
  return sql

def getAccountsSQL(accounts):
  sql = 'INSERT INTO `accounts` (`id`,`accountnumber`,`availablecredit`,`balance`,`bankid`,`certaindate`,`credentialsid`,`excluded`,`favored`,`identifiers`,`name`,`ownership`,`payload`,`type`,`userid`,`usermodifiedname`,`usermodifiedtype`) VALUES '
  f = getFormat(len(accountTemplate))
  for i,a in enumerate(accounts):
    sql += f.format(
      _s(a['id']),
      _n(a['accountnumber']),
      _n(a['availablecredit']),
      _n(a['balance']),
      _s(a['bankid']),
      _s(a['certaindate']),
      _s(a['credentialsid']),
      _b(a['excluded']),
      _b(a['favored']),
      _s(a['identifiers']),
      _s(a['name']),
      _n(a['ownership']),
      _s(a['payload']),
      _s(a['type']),
      _s(a['userid']),
      _b(a['usermodifiedname']),
      _b(a['usermodifiedtype']))
    sql += lineEnd(i, len(accounts))
  return sql

def main(argv):
  userCount = int(argv[0])

  users = []
  credentials = []
  accounts = []

  for i in range(0,userCount):
    u = userTemplate.copy()
    u['id'] = tinkuuid()
    u['username'] = 'abnamro-test-{0}'.format(i)
    users.append(u)

    c = credentialsTemplate.copy()
    c['id'] = tinkuuid()
    c['userid'] = u['id']
    credentials.append(c)

    a = accountTemplate.copy()
    a['id'] = tinkuuid()
    a['credentialsid'] = c['id']
    a['userid'] = u['id']
    a['accountnumber'] = accountnumber + 1 + i
    a['bankid'] = str(a['accountnumber'])
    accounts.append(a)

  print getUsersSQL(users)
  print getCredentialsSQL(credentials)
  print getAccountsSQL(accounts)



if __name__ == "__main__":
  main(sys.argv[1:])
