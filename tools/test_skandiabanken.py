import requests
from BeautifulSoup import BeautifulSoup
import urlparse

proxy = 'http://127.0.0.1:8888'
proxies = {
    'http': proxy,
    'https': proxy,
}

def main():
  x_smartrefill_headers = {
    'x-smartrefill-version': '411',
    'x-smartrefill-language': 'sv',
    'x-smartrefill-inflow': 'iphone',
    'x-smartrefill-device': '9CF08BF2-3D49-4739-A60C-11CCF3E8EFBC',
    'x-smartrefill-api-version': '2',
    'x-smartrefill-company': 'SKANDIABANKEN',
    'x-smartrefill-country': 'SE',
    'x-smartrefill-marketing-version': '2.4.2',
    'x-smartrefill-application': 'se.skandia.skandia',
  }
  r = requests.get('https://service2.smartrefill.se/BankServices/rest/bank/SE/SKANDIABANKEN/', headers=x_smartrefill_headers)
  loginMethods = r.json()['loginMethods']
  loginUrl = [loginMethod['loginUrl'] for loginMethod in loginMethods if loginMethod['typeOfLogin']==3][0]

  browserClient = requests.Session()
  r = browserClient.get(loginUrl)
  soup = BeautifulSoup(r.text)

  rvtokenTag = soup.find(id='loginForm').find(attrs={'name': '__RequestVerificationToken'})
  rvtoken = dict(rvtokenTag.attrs)['value']
  print "RequestVerificationToken:", rvtoken

  data = {
    '__RequestVerificationToken': rvtoken,
    'LoginMobilPinModel.SocialSecurityNumber': '8210303999',
    'LoginMobilPinModel.Pin': '2473',
  }
  # Some of there headers might not be necessary. I added them all before finding a minor typo in the parameters above.
  headers = {
  }
  r = browserClient.post('https://secure3.skandiabanken.se/Login/LoginMobilPin/Authenticate',
             data=data, headers=headers, proxies=proxies, verify=False)
  #r = s.post('http://httpbin.org/post', data=data, headers=headers)
  ticketUrl = r.json()['TicketUrl']
  print "Ticket URL:", ticketUrl

  headers = {
    #'User-Agent': 'Skandia/411 CFNetwork/672.1.13 Darwin/14.0.0',
  }
  urlPieces = urlparse.urlparse(ticketUrl)
  url = "{0.scheme}://{0.netloc}{0.path}".format(urlPieces)
  queryParams = urlparse.parse_qs(urlPieces.query)
  iphoneClient = requests.Session()
  print "url:", url
  print "params:", queryParams
  r = iphoneClient.get(url, params=queryParams, headers=headers, proxies=proxies, verify=False, allow_redirects=False)
  print "Final url:", r.url
  assert r.status_code == 302, r.status_code

  urlPieces = urlparse.urlparse(r.headers['Location'])
  urlQuery = urlparse.parse_qs(urlPieces.query)
  url = "https://service2.smartrefill.se/BankServices/rest/bank/SE/SKANDIABANKEN/login"
  params = {
      'state': urlQuery['state'],
      'access_code': urlQuery['code'],
  }
  headers = {
    #'User-Agent': 'Skandia/411 CFNetwork/672.1.13 Darwin/14.0.0',
  }
  headers.update(x_smartrefill_headers)   # Fails without these.
  r = iphoneClient.post(url, params=params, headers=headers)
  assert "securityToken" in r.text, r.text
  assert r.status_code == 201, r.status_code
  userid = r.json()['id']

  # Logged in.

  # Fetching data
  r = iphoneClient.get('https://service2.smartrefill.se/BankServices/rest/bank/SE/SKANDIABANKEN/customer/{0}?'.format(userid))
  data = r.json()
  assert 'phoneNumber' in data, str(data)

if __name__=='__main__':
  main()
