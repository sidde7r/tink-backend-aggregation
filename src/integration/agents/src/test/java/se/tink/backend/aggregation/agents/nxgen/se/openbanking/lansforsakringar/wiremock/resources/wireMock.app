

REQUEST 1
POST https://api.bank.lansforsakringar.se:443/openbanking/ano/v2/consents
Accept: application/json
Content-Type: application/json
PSU-IP-Address: 127.0.0.1
PSU-User-Agent: Desktop Mode
TPP-NOK-Redirect-URI: https://127.0.0.1:7357/api/v1/thirdparty/callback
TPP-Redirect-URI: https://127.0.0.1:7357/api/v1/thirdparty/callback


RESPONSE 1
201
Date: Wed, 15 Jul 2020 12:19:38 GMT
Date: Wed, 15 Jul 2020 12:19:38 GMT
Content-Type: application/json;charset=utf-8

{"consentId":"consentId","consentStatus":"RECEIVED","authorisationId":"authorisationId_1","_links":{"self":{"href":"/openbanking/sec/v2/consents/consentId"},"status":{"href":"/openbanking/sec/v2/consents/consentId/status"},"scaOAuth":{"href":"https://secure397.lansforsakringar.se/.well-known/openid-configuration"},"scaStatus":{"href":"/openbanking/sec/v2/consents/consentId/authorisations/authorisationId_1"}}}

REQUEST 2
POST https://api.bank.lansforsakringar.se:443/openbanking/ano/v2/consents/consentId/authorisations
Accept: application/json
Content-Type: application/json
PSU-IP-Address: 127.0.0.1
PSU-User-Agent: Desktop Mode
TPP-NOK-Redirect-URI: https://127.0.0.1:7357/api/v1/thirdparty/callback
TPP-Redirect-URI: https://127.0.0.1:7357/api/v1/thirdparty/callback


RESPONSE 2
201
Date: Wed, 15 Jul 2020 12:19:39 GMT
Date: Wed, 15 Jul 2020 12:19:39 GMT
Content-Type: application/json;charset=utf-8

{"scaStatus":"RECEIVED","consentId":"consentId","authorisationId":"authorisationId","_links":{"self":{"href":"/openbanking/sec/v2/consents/consentId"},"status":{"href":"/openbanking/sec/v2/consents/consentId/status"},"scaOAuth":{"href":"https://secure397.lansforsakringar.se/.well-known/openid-configuration"},"scaStatus":{"href":"/openbanking/sec/v2/consents/consentId/authorisations/authorisationId"}}}

REQUEST 3
POST https://secure397.lansforsakringar.se/as/token.oauth2
Content-Type: application/x-www-form-urlencoded
Accept: application/json

client_id=dummyClient&grant_type=authorization_code&code=dummyCode&client_secret=dummySecret&redirect_uri=https%3A%2F%2F127.0.0.1%3A7357%2Fapi%2Fv1%2Fthirdparty%2Fcallback

RESPONSE 3
200
Date: Wed, 15 Jul 2020 12:20:11 GMT
Date: Wed, 15 Jul 2020 12:20:12 GMT
Expires: Thu, 01 Jan 1970 00:00:00 GMT
Content-Type: application/json;charset=utf-8

{"access_token":"dummyToken","refresh_token":"dummyRefreshToken","token_type":"Bearer","expires_in":299}

REQUEST 4
GET https://api.bank.lansforsakringar.se:443/openbanking/ais/v1/accounts
Accept: application/json


RESPONSE 4
200
Date: Wed, 15 Jul 2020 12:51:03 GMT
Date: Wed, 15 Jul 2020 12:51:03 GMT
Content-Type: application/json;charset=utf-8
Cache-Control: no-cache, no-store, private, proxy-revalidate
Content-Length: 2006

{"accounts":[{"resourceId":"resourceId","bban":"90200044000","name":"Sparkonto","product":"Sparkonto","currency":"SEK","allowedTransactionTypes":["DOMESTIC_CREDIT_TRANSFERS"],"_links":{"account":{"href":"/openbanking/ais/v1/accounts/resourceId"},"transactions":{"href":"/openbanking/ais/v1/accounts/resourceId/transactions"},"balances":{"href":"/openbanking/ais/v1/accounts/resourceId/balances"}}},{"resourceId":"resourceId_2","bban":"90000670000","name":"Privatkonto","product":"Privatkonto","currency":"SEK","allowedTransactionTypes":["DOMESTIC_CREDIT_TRANSFERS","DOMESTIC_GIROS","CROSS_BORDER_CREDIT_TRANSFERS"],"_links":{"account":{"href":"/openbanking/ais/v1/accounts/resourceId_2"},"transactions":{"href":"/openbanking/ais/v1/accounts/resourceId_2/transactions"},"balances":{"href":"/openbanking/ais/v1/accounts/resourceId_2/balances"}}},{"resourceId":"resourceId_3","bban":"000000","pan":["****1111"],"product":"Kreditkort Privat","currency":"SEK","_links":{"account":{"href":"/openbanking/ais/v1/accounts/resourceId_3"},"transactions":{"href":"/openbanking/ais/v1/accounts/resourceId_3/transactions"},"balances":{"href":"/openbanking/ais/v1/accounts/resourceId_3/balances"}}}]}

REQUEST 5
GET https://api.bank.lansforsakringar.se:443/openbanking/ais/v1/accounts/resourceId/balances
Accept: application/json
Content-Type: application/json,application/x-www-form-urlencoded
PSU-IP-Address: 127.0.0.1
PSU-User-Agent: Desktop Mode
TPP-NOK-Redirect-URI: https://127.0.0.1:7357/api/v1/thirdparty/callback

RESPONSE 5
200
Date: Wed, 15 Jul 2020 12:51:03 GMT
Date: Wed, 15 Jul 2020 12:51:03 GMT
Content-Type: application/json;charset=utf-8
Cache-Control: no-cache, no-store, private, proxy-revalidate
Content-Length: 2006

{"balances":[{"balanceAmount":{"amount":5.00,"currency":"SEK"},"balanceType":"AUTHORIZED"},{"balanceAmount":{"amount":5.00,"currency":"SEK"},"balanceType":"EXPECTED"}]}

REQUEST 6
GET https://api.bank.lansforsakringar.se:443/openbanking/ais/v1/accounts/resourceId_2/balances
Accept: application/json
Content-Type: application/json,application/x-www-form-urlencoded
PSU-IP-Address: 127.0.0.1
PSU-User-Agent: Desktop Mode
TPP-NOK-Redirect-URI: https://127.0.0.1:7357/api/v1/thirdparty/callback

RESPONSE 6
200
Date: Wed, 15 Jul 2020 12:51:03 GMT
Date: Wed, 15 Jul 2020 12:51:03 GMT
Content-Type: application/json;charset=utf-8
Cache-Control: no-cache, no-store, private, proxy-revalidate
Content-Length: 2006

{"balances":[{"balanceAmount":{"amount":6098.39,"currency":"SEK"},"balanceType":"AUTHORIZED"},{"balanceAmount":{"amount":6098.39,"currency":"SEK"},"balanceType":"EXPECTED"}]}

REQUEST 7
GET https://api.bank.lansforsakringar.se:443/openbanking/ais/v1/accounts/resourceId_3/balances
Accept: application/json
Content-Type: application/json,application/x-www-form-urlencoded
PSU-IP-Address: 127.0.0.1
PSU-User-Agent: Desktop Mode
TPP-NOK-Redirect-URI: https://127.0.0.1:7357/api/v1/thirdparty/callback

RESPONSE 7
200
Date: Wed, 15 Jul 2020 12:51:03 GMT
Date: Wed, 15 Jul 2020 12:51:03 GMT
Content-Type: application/json;charset=utf-8
Cache-Control: no-cache, no-store, private, proxy-revalidate
Content-Length: 2006

{"balances":[{"balanceAmount":{"amount":28335.45,"currency":"SEK"},"balanceType":"AUTHORIZED"}]}

REQUEST 8
GET https://api.bank.lansforsakringar.se:443/openbanking/ais/v1/accounts/resourceId/transactions?dateFrom=1991-03-10&bookingStatus=both
Accept: application/json
Content-Type: application/json
PSU-IP-Address: 127.0.0.1
PSU-User-Agent: Desktop Mode
TPP-NOK-Redirect-URI: https://127.0.0.1:7357/api/v1/thirdparty/callback

RESPONSE 8
200
Date: Wed, 15 Jul 2020 12:51:04 GMT
Date: Wed, 15 Jul 2020 12:51:04 GMT
Content-Type: application/json;charset=utf-8
Cache-Control: no-cache, no-store, private, proxy-revalidate
Content-Length: 7019

{"account":{"bban":"90200044000","currency":"SEK"},"transactions":{"pending":[],"booked":[{"entryReference":"316465105-25","text":"text","bookingDate":"2020-07-10","transactionDate":"2020-07-10","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":1.00,"currency":"SEK"}},{"entryReference":"282050285-25","text":"text","bookingDate":"2020-05-11","transactionDate":"2020-05-11","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-1.00,"currency":"SEK"}},{"entryReference":"280158899-25","text":"text","bookingDate":"2020-05-05","transactionDate":"2020-05-05","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":5.00,"currency":"SEK"}},{"entryReference":"279399909-25","text":"text","bookingDate":"2020-05-04","transactionDate":"2020-05-04","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-2380.08,"currency":"SEK"}},{"entryReference":"264350632-25","text":"text","bookingDate":"2020-04-07","transactionDate":"2020-04-07","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":1864.00,"currency":"SEK"}},{"entryReference":"252726722-25","text":"text","bookingDate":"2020-03-19","transactionDate":"2020-03-19","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-1740.00,"currency":"SEK"}},{"entryReference":"249160343-25","text":"text","bookingDate":"2020-03-17","transactionDate":"2020-03-17","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":1.00,"currency":"SEK"}},{"entryReference":"250556244-25","text":"text","bookingDate":"2020-03-14","transactionDate":"2020-03-14","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-1000.00,"currency":"SEK"}},{"entryReference":"249856689-25","text":"text","bookingDate":"2020-03-12","transactionDate":"2020-03-12","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":3.00,"currency":"SEK"}},{"entryReference":"249158245-25","text":"text","bookingDate":"2020-03-10","transactionDate":"2020-03-10","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-4250.00,"currency":"SEK"}},{"entryReference":"249156629-25","text":"text","bookingDate":"2020-03-10","transactionDate":"2020-03-10","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":4108.08,"currency":"SEK"}},{"entryReference":"249155498-25","text":"text","bookingDate":"2020-03-10","transactionDate":"2020-03-10","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":1.00,"currency":"SEK"}},{"entryReference":"247162912-25","text":"text","bookingDate":"2020-03-04","transactionDate":"2020-03-04","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-1303.00,"currency":"SEK"}},{"entryReference":"246505590-25","text":"text","bookingDate":"2020-03-02","transactionDate":"2020-03-02","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-15899.00,"currency":"SEK"}},{"entryReference":"242330892-25","text":"text","bookingDate":"2020-02-27","transactionDate":"2020-02-27","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-5405.00,"currency":"SEK"}},{"entryReference":"239900924-25","text":"text","bookingDate":"2020-02-25","transactionDate":"2020-02-25","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-7500.00,"currency":"SEK"}},{"entryReference":"236010407-25","text":"text","bookingDate":"2020-02-18","transactionDate":"2020-02-18","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-56000.00,"currency":"SEK"}},{"entryReference":"233388744-25","text":"text","bookingDate":"2020-02-11","transactionDate":"2020-02-11","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-1.00,"currency":"SEK"}},{"entryReference":"233386285-25","text":"text","bookingDate":"2020-02-11","transactionDate":"2020-02-11","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-1.00,"currency":"SEK"}},{"entryReference":"233385502-25","text":"text","bookingDate":"2020-02-11","transactionDate":"2020-02-11","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-1.00,"currency":"SEK"}},{"entryReference":"233051784-25","text":"text","bookingDate":"2020-02-10","transactionDate":"2020-02-10","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-1.00,"currency":"SEK"}},{"entryReference":"231775523-25","text":"text","bookingDate":"2020-02-06","transactionDate":"2020-02-06","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-29000.00,"currency":"SEK"}},{"entryReference":"229565308-25","text":"text","bookingDate":"2020-02-01","transactionDate":"2020-02-01","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-6500.00,"currency":"SEK"}},{"entryReference":"226064420-25","text":"text","bookingDate":"2020-01-27","transactionDate":"2020-01-27","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":125000.00,"currency":"SEK"}},{"entryReference":"218383492-25","text":"text","bookingDate":"2020-01-15","transactionDate":"2020-01-15","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":1.00,"currency":"SEK"}},{"entryReference":"205509124-25","text":"text","bookingDate":"2019-12-20","transactionDate":"2019-12-20","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":1.00,"currency":"SEK"}},{"entryReference":"205406930-25","text":"text","bookingDate":"2019-12-20","transactionDate":"2019-12-20","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":1.00,"currency":"SEK"}},{"entryReference":"182984221-25","text":"text","bookingDate":"2019-11-08","transactionDate":"2019-11-08","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-5.00,"currency":"SEK"}},{"entryReference":"182185888-25","text":"text","bookingDate":"2019-11-06","transactionDate":"2019-11-06","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":5.00,"currency":"SEK"}},{"entryReference":"181877494-25","text":"text","bookingDate":"2019-11-05","transactionDate":"2019-11-05","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-1.00,"currency":"SEK"}}],"_links":{"first":{"href":"/openbanking/ais/v1/accounts/resourceId/transactions?&dateFrom=1991-03-10&bookingStatus=both"},"next":{"href":"/openbanking/ais/v1/accounts/resourceId/transactions?&dateFrom=1991-03-10&bookingStatus=both&pager=5yv8cJ-1I2xlVuNKNiMRPQ"}}}}

REQUEST 9
GET https://api.bank.lansforsakringar.se:443/openbanking/ais/v1/accounts/resourceId/transactions?&dateFrom=1991-03-10&bookingStatus=both&pager=5yv8cJ-1I2xlVuNKNiMRPQ
Accept: application/json
Content-Type: application/json
PSU-IP-Address: 127.0.0.1
PSU-User-Agent: Desktop Mode
TPP-NOK-Redirect-URI: https://127.0.0.1:7357/api/v1/thirdparty/callback

RESPONSE 9
200
Date: Wed, 15 Jul 2020 12:51:05 GMT
Date: Wed, 15 Jul 2020 12:51:05 GMT
Content-Type: application/json;charset=utf-8
Cache-Control: no-cache, no-store, private, proxy-revalidate

{"account":{"bban":"90200044000","currency":"SEK"},"transactions":{"pending":[],"booked":[{"entryReference":"316465105-25","text":"text","bookingDate":"2020-07-10","transactionDate":"2020-07-10","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":1.00,"currency":"SEK"}},{"entryReference":"282050285-25","text":"text","bookingDate":"2020-05-11","transactionDate":"2020-05-11","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-1.00,"currency":"SEK"}},{"entryReference":"280158899-25","text":"text","bookingDate":"2020-05-05","transactionDate":"2020-05-05","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":5.00,"currency":"SEK"}},{"entryReference":"279399909-25","text":"text","bookingDate":"2020-05-04","transactionDate":"2020-05-04","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-2380.08,"currency":"SEK"}},{"entryReference":"264350632-25","text":"text","bookingDate":"2020-04-07","transactionDate":"2020-04-07","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":1864.00,"currency":"SEK"}},{"entryReference":"252726722-25","text":"text","bookingDate":"2020-03-19","transactionDate":"2020-03-19","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-1740.00,"currency":"SEK"}},{"entryReference":"249160343-25","text":"text","bookingDate":"2020-03-17","transactionDate":"2020-03-17","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":1.00,"currency":"SEK"}},{"entryReference":"250556244-25","text":"text","bookingDate":"2020-03-14","transactionDate":"2020-03-14","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-1000.00,"currency":"SEK"}},{"entryReference":"249856689-25","text":"text","bookingDate":"2020-03-12","transactionDate":"2020-03-12","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":3.00,"currency":"SEK"}},{"entryReference":"249158245-25","text":"text","bookingDate":"2020-03-10","transactionDate":"2020-03-10","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-4250.00,"currency":"SEK"}},{"entryReference":"249156629-25","text":"text","bookingDate":"2020-03-10","transactionDate":"2020-03-10","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":4108.08,"currency":"SEK"}},{"entryReference":"249155498-25","text":"text","bookingDate":"2020-03-10","transactionDate":"2020-03-10","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":1.00,"currency":"SEK"}},{"entryReference":"247162912-25","text":"text","bookingDate":"2020-03-04","transactionDate":"2020-03-04","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-1303.00,"currency":"SEK"}},{"entryReference":"246505590-25","text":"text","bookingDate":"2020-03-02","transactionDate":"2020-03-02","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-15899.00,"currency":"SEK"}},{"entryReference":"242330892-25","text":"text","bookingDate":"2020-02-27","transactionDate":"2020-02-27","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-5405.00,"currency":"SEK"}},{"entryReference":"239900924-25","text":"text","bookingDate":"2020-02-25","transactionDate":"2020-02-25","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-7500.00,"currency":"SEK"}},{"entryReference":"236010407-25","text":"text","bookingDate":"2020-02-18","transactionDate":"2020-02-18","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-56000.00,"currency":"SEK"}},{"entryReference":"233388744-25","text":"text","bookingDate":"2020-02-11","transactionDate":"2020-02-11","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-1.00,"currency":"SEK"}},{"entryReference":"233386285-25","text":"text","bookingDate":"2020-02-11","transactionDate":"2020-02-11","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-1.00,"currency":"SEK"}},{"entryReference":"233385502-25","text":"text","bookingDate":"2020-02-11","transactionDate":"2020-02-11","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-1.00,"currency":"SEK"}},{"entryReference":"233051784-25","text":"text","bookingDate":"2020-02-10","transactionDate":"2020-02-10","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-1.00,"currency":"SEK"}},{"entryReference":"231775523-25","text":"text","bookingDate":"2020-02-06","transactionDate":"2020-02-06","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-29000.00,"currency":"SEK"}},{"entryReference":"229565308-25","text":"text","bookingDate":"2020-02-01","transactionDate":"2020-02-01","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-6500.00,"currency":"SEK"}},{"entryReference":"226064420-25","text":"text","bookingDate":"2020-01-27","transactionDate":"2020-01-27","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":125000.00,"currency":"SEK"}},{"entryReference":"218383492-25","text":"text","bookingDate":"2020-01-15","transactionDate":"2020-01-15","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":1.00,"currency":"SEK"}},{"entryReference":"205509124-25","text":"text","bookingDate":"2019-12-20","transactionDate":"2019-12-20","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":1.00,"currency":"SEK"}},{"entryReference":"205406930-25","text":"text","bookingDate":"2019-12-20","transactionDate":"2019-12-20","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":1.00,"currency":"SEK"}},{"entryReference":"182984221-25","text":"text","bookingDate":"2019-11-08","transactionDate":"2019-11-08","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-5.00,"currency":"SEK"}},{"entryReference":"182185888-25","text":"text","bookingDate":"2019-11-06","transactionDate":"2019-11-06","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":5.00,"currency":"SEK"}},{"entryReference":"181877494-25","text":"text","bookingDate":"2019-11-05","transactionDate":"2019-11-05","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-1.00,"currency":"SEK"}}],"_links":{"first":{"href":"/openbanking/ais/v1/accounts/resourceId/transactions?&dateFrom=1991-03-10&bookingStatus=both"}}}}


REQUEST 10
GET https://api.bank.lansforsakringar.se:443/openbanking/ais/v1/accounts/resourceId_2/transactions?dateFrom=1991-03-10&bookingStatus=both
Accept: application/json
Content-Type: application/json
PSU-IP-Address: 127.0.0.1
PSU-User-Agent: Desktop Mode
TPP-NOK-Redirect-URI: https://127.0.0.1:7357/api/v1/thirdparty/callback

RESPONSE 10
200
Date: Wed, 15 Jul 2020 12:51:06 GMT
Date: Wed, 15 Jul 2020 12:51:06 GMT
Content-Type: application/json;charset=utf-8
Cache-Control: no-cache, no-store, private, proxy-revalidate
Content-Length: 7156

{"account":{"bban":"90000670000","currency":"SEK"},"transactions":{"pending":[{"entryReference":"283954799-0000","text":"text","bookingDate":"2020-07-15","transactionDate":"2020-07-14","remittanceInformationUnstructured":"Pending","transactionAmount":{"amount":-70,"currency":"SEK"}},{"entryReference":"283862998-0000","text":"text","bookingDate":"2020-07-15","transactionDate":"2020-07-14","remittanceInformationUnstructured":"Pending2","transactionAmount":{"amount":-529.87,"currency":"SEK"}},{"entryReference":"282639720-0000","text":"text","bookingDate":"2020-07-14","transactionDate":"2020-07-12","remittanceInformationUnstructured":"Pending3","transactionAmount":{"amount":-475,"currency":"SEK"}},{"entryReference":"35547139-000","text":"text","bookingDate":"2020-07-13","transactionDate":"2020-07-13","remittanceInformationUnstructured":"Pending4","transactionAmount":{"amount":-778,"currency":"SEK"}}],"booked":[{"entryReference":"283954799-1516","text":"text","bookingDate":"2020-07-15","transactionDate":"2020-07-14","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-70,"currency":"SEK"}},{"entryReference":"283862998-1516","text":"text","bookingDate":"2020-07-15","transactionDate":"2020-07-14","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-529.87,"currency":"SEK"}},{"entryReference":"282639720-1516","text":"text","bookingDate":"2020-07-14","transactionDate":"2020-07-12","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-475,"currency":"SEK"}},{"entryReference":"35547139-202","text":"text","bookingDate":"2020-07-13","transactionDate":"2020-07-13","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-778,"currency":"SEK"}},{"entryReference":"316465105-25","text":"text","bookingDate":"2020-07-10","transactionDate":"2020-07-10","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-1,"currency":"SEK"}},{"entryReference":"316459385-25","text":"text","bookingDate":"2020-07-10","transactionDate":"2020-07-10","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-3,"currency":"SEK"}},{"entryReference":"316039541-25","text":"text","bookingDate":"2020-07-09","transactionDate":"2020-07-09","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-120,"currency":"SEK"}},{"entryReference":"314883623-25","text":"text","bookingDate":"2020-07-06","transactionDate":"2020-07-06","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-400,"currency":"SEK"}},{"entryReference":"314000614-25","text":"text","bookingDate":"2020-07-03","transactionDate":"2020-07-03","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":7303,"currency":"SEK"}},{"entryReference":"313904314-25","text":"text","bookingDate":"2020-07-03","transactionDate":"2020-07-03","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-2174,"currency":"SEK"}},{"entryReference":"313903333-25","text":"text","bookingDate":"2020-07-03","transactionDate":"2020-07-03","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-4687.96,"currency":"SEK"}},{"entryReference":"313497982-25","text":"text","bookingDate":"2020-07-03","transactionDate":"2020-07-03","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-191.51,"currency":"SEK"}},{"entryReference":"313503039-25","text":"text","bookingDate":"2020-07-03","transactionDate":"2020-07-03","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-1,"currency":"SEK"}},{"entryReference":"163678901-1625","text":"text,ELIAS DANI","bookingDate":"2020-07-02","transactionDate":"2020-07-02","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-457,"currency":"SEK"}},{"entryReference":"275426450-1516","text":"text","bookingDate":"2020-07-01","transactionDate":"2020-06-30","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-100,"currency":"SEK"}},{"entryReference":"34288619-202","text":"text","bookingDate":"2020-06-30","transactionDate":"2020-06-30","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-3814,"currency":"SEK"}},{"entryReference":"34288618-202","text":"text","bookingDate":"2020-06-30","transactionDate":"2020-06-30","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-235,"currency":"SEK"}},{"entryReference":"34288617-202","text":"text","bookingDate":"2020-06-30","transactionDate":"2020-06-30","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-151,"currency":"SEK"}},{"entryReference":"307746004-25","text":"text","bookingDate":"2020-06-30","transactionDate":"2020-06-30","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-1119.58,"currency":"SEK"}},{"entryReference":"34114073-202","text":"text","bookingDate":"2020-06-29","transactionDate":"2020-06-29","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-350,"currency":"SEK"}},{"entryReference":"34114072-202","text":"text","bookingDate":"2020-06-29","transactionDate":"2020-06-29","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-112,"currency":"SEK"}},{"entryReference":"308426324-25","text":"text","bookingDate":"2020-06-26","transactionDate":"2020-06-26","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-12312,"currency":"SEK"}},{"entryReference":"162271556-1625","text":"text","bookingDate":"2020-06-26","transactionDate":"2020-06-26","transactionAmount":{"amount":4200,"currency":"SEK"}},{"entryReference":"307745965-25","text":"text","bookingDate":"2020-06-26","transactionDate":"2020-06-26","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-4500,"currency":"SEK"}},{"entryReference":"33376740-202","text":"text","bookingDate":"2020-06-26","transactionDate":"2020-06-26","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-109,"currency":"SEK"}},{"entryReference":"305557658-25","text":"text","bookingDate":"2020-06-24","transactionDate":"2020-06-24","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":26867,"currency":"SEK"}},{"entryReference":"268820312-1516","text":"text","bookingDate":"2020-06-19","transactionDate":"2020-06-18","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-160.95,"currency":"SEK"}},{"entryReference":"159665403-1625","text":"text","bookingDate":"2020-06-14","transactionDate":"2020-06-14","transactionAmount":{"amount":150,"currency":"SEK"}},{"entryReference":"159379451-1625","text":"text,JENS","bookingDate":"2020-06-12","transactionDate":"2020-06-12","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":-4250,"currency":"SEK"}},{"entryReference":"159378442-1625","text":"text,CHRISTOFF","bookingDate":"2020-06-12","transactionDate":"2020-06-12","remittanceInformationUnstructured":"remittanceInformationUnstructured","transactionAmount":{"amount":2000,"currency":"SEK"}}],"_links":{"first":{"href":"/openbanking/ais/v1/accounts/resourceId_2/transactions?&dateFrom=1991-03-10&bookingStatus=both"}}}}


REQUEST 11
GET https://api.bank.lansforsakringar.se:443/openbanking/ais/v1/accounts/resourceId_3/transactions?dateFrom=1991-03-10&bookingStatus=both
Accept: application/json
Content-Type: application/json
PSU-IP-Address: 127.0.0.1
PSU-User-Agent: Desktop Mode
TPP-NOK-Redirect-URI: https://127.0.0.1:7357/api/v1/thirdparty/callback

RESPONSE 11
200
Date: Wed, 15 Jul 2020 12:51:08 GMT
Date: Wed, 15 Jul 2020 12:51:08 GMT
Content-Type: application/json;charset=utf-8
Cache-Control: no-cache, no-store, private, proxy-revalidate

{"account":{"bban":"000000","currency":"SEK"},"transactions":{"pending":[],"booked":[{"entryReference":"2019501730151122","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-11","transactionAmount":{"amount":-220.00,"currency":"SEK"}},{"entryReference":"2019501730151121","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-10","transactionAmount":{"amount":-104.13,"currency":"SEK"}},{"entryReference":"2019201730110244","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-09","transactionAmount":{"amount":-49.00,"currency":"SEK"}},{"entryReference":"2019101730064587","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-08","transactionAmount":{"amount":-3995.00,"currency":"SEK"}},{"entryReference":"2019001730062791","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-07","transactionAmount":{"amount":-129.00,"currency":"SEK"}},{"entryReference":"2019001730062790","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-07","transactionAmount":{"amount":-110.00,"currency":"SEK"}},{"entryReference":"2019001730062789","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-07","transactionAmount":{"amount":-109.00,"currency":"SEK"}},{"entryReference":"2018801730162603","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-05","transactionAmount":{"amount":-175.00,"currency":"SEK"}},{"entryReference":"2018801730162602","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-05","transactionAmount":{"amount":-49.00,"currency":"SEK"}},{"entryReference":"2018801730162601","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-04","transactionAmount":{"amount":-521.60,"currency":"SEK"}},{"entryReference":"2018801730162600","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-04","transactionAmount":{"amount":-274.00,"currency":"SEK"}},{"entryReference":"2018801730162599","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-03","transactionAmount":{"amount":-138.40,"currency":"SEK"}},{"entryReference":"2018801730162598","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-03","transactionAmount":{"amount":-85.00,"currency":"SEK"}},{"entryReference":"2018901730077617","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-02","transactionAmount":{"amount":-11849.00,"currency":"SEK"}},{"entryReference":"2018501730087481","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-02","transactionAmount":{"amount":-115.24,"currency":"SEK"}},{"entryReference":"2018501730087480","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-02","transactionAmount":{"amount":-90.00,"currency":"SEK"}},{"entryReference":"2018401730054632","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-01","transactionAmount":{"amount":-184.51,"currency":"SEK"}},{"entryReference":"2018201730127951","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-29","transactionAmount":{"amount":-45.98,"currency":"SEK"}},{"entryReference":"2017701730103206","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-24","transactionAmount":{"amount":-29.00,"currency":"SEK"}},{"entryReference":"2017401730150718","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-21","transactionAmount":{"amount":-948.00,"currency":"SEK"}},{"entryReference":"2017401730150717","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-21","transactionAmount":{"amount":-597.00,"currency":"SEK"}},{"entryReference":"2017401730150716","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-21","transactionAmount":{"amount":-525.00,"currency":"SEK"}},{"entryReference":"2017401730150715","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-21","transactionAmount":{"amount":-149.00,"currency":"SEK"}},{"entryReference":"2017401730150714","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-21","transactionAmount":{"amount":-139.00,"currency":"SEK"}},{"entryReference":"2017401730150713","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-21","transactionAmount":{"amount":-129.81,"currency":"SEK"}},{"entryReference":"2017401730150712","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-21","transactionAmount":{"amount":-120.00,"currency":"SEK"}},{"entryReference":"2017401730150711","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-21","transactionAmount":{"amount":-111.00,"currency":"SEK"}},{"entryReference":"2017401730150710","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-21","transactionAmount":{"amount":-65.43,"currency":"SEK"}},{"entryReference":"2017401730150709","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-21","transactionAmount":{"amount":-37.00,"currency":"SEK"}},{"entryReference":"2017401730150708","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-21","transactionAmount":{"amount":-37.00,"currency":"SEK"}}],"_links":{"first":{"href":"/openbanking/ais/v1/accounts/resourceId_3/transactions?&dateFrom=1991-03-10&bookingStatus=both"},"next":{"href":"/openbanking/ais/v1/accounts/resourceId_3/transactions?&dateFrom=1991-03-10&bookingStatus=both&pager=5yv8cJ-1I2xlVuNKNiMRPQ"}}}}

REQUEST 12
GET https://api.bank.lansforsakringar.se:443/openbanking/ais/v1/accounts/resourceId_3/transactions?&dateFrom=1991-03-10&bookingStatus=both&pager=5yv8cJ-1I2xlVuNKNiMRPQ
Accept: application/json
Content-Type: application/json
PSU-IP-Address: 127.0.0.1
PSU-User-Agent: Desktop Mode
TPP-NOK-Redirect-URI: https://127.0.0.1:7357/api/v1/thirdparty/callback

RESPONSE 12
200
Date: Wed, 15 Jul 2020 12:51:09 GMT
Date: Wed, 15 Jul 2020 12:51:09 GMT
Content-Type: application/json;charset=utf-8
Cache-Control: no-cache, no-store, private, proxy-revalidate

{"account":{"bban":"000000","currency":"SEK"},"transactions":{"pending":[],"booked":[{"entryReference":"2019501730151122","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-11","transactionAmount":{"amount":-220.00,"currency":"SEK"}},{"entryReference":"2019501730151121","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-10","transactionAmount":{"amount":-104.13,"currency":"SEK"}},{"entryReference":"2019201730110244","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-09","transactionAmount":{"amount":-49.00,"currency":"SEK"}},{"entryReference":"2019101730064587","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-08","transactionAmount":{"amount":-3995.00,"currency":"SEK"}},{"entryReference":"2019001730062791","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-07","transactionAmount":{"amount":-129.00,"currency":"SEK"}},{"entryReference":"2019001730062790","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-07","transactionAmount":{"amount":-110.00,"currency":"SEK"}},{"entryReference":"2019001730062789","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-07","transactionAmount":{"amount":-109.00,"currency":"SEK"}},{"entryReference":"2018801730162603","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-05","transactionAmount":{"amount":-175.00,"currency":"SEK"}},{"entryReference":"2018801730162602","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-05","transactionAmount":{"amount":-49.00,"currency":"SEK"}},{"entryReference":"2018801730162601","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-04","transactionAmount":{"amount":-521.60,"currency":"SEK"}},{"entryReference":"2018801730162600","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-04","transactionAmount":{"amount":-274.00,"currency":"SEK"}},{"entryReference":"2018801730162599","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-03","transactionAmount":{"amount":-138.40,"currency":"SEK"}},{"entryReference":"2018801730162598","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-03","transactionAmount":{"amount":-85.00,"currency":"SEK"}},{"entryReference":"2018901730077617","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-02","transactionAmount":{"amount":-11849.00,"currency":"SEK"}},{"entryReference":"2018501730087481","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-02","transactionAmount":{"amount":-115.24,"currency":"SEK"}},{"entryReference":"2018501730087480","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-02","transactionAmount":{"amount":-90.00,"currency":"SEK"}},{"entryReference":"2018401730054632","text":"Köp","merchantName":"merchantName","transactionDate":"2020-07-01","transactionAmount":{"amount":-184.51,"currency":"SEK"}},{"entryReference":"2018201730127951","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-29","transactionAmount":{"amount":-45.98,"currency":"SEK"}},{"entryReference":"2017701730103206","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-24","transactionAmount":{"amount":-29.00,"currency":"SEK"}},{"entryReference":"2017401730150718","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-21","transactionAmount":{"amount":-948.00,"currency":"SEK"}},{"entryReference":"2017401730150717","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-21","transactionAmount":{"amount":-597.00,"currency":"SEK"}},{"entryReference":"2017401730150716","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-21","transactionAmount":{"amount":-525.00,"currency":"SEK"}},{"entryReference":"2017401730150715","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-21","transactionAmount":{"amount":-149.00,"currency":"SEK"}},{"entryReference":"2017401730150714","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-21","transactionAmount":{"amount":-139.00,"currency":"SEK"}},{"entryReference":"2017401730150713","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-21","transactionAmount":{"amount":-129.81,"currency":"SEK"}},{"entryReference":"2017401730150712","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-21","transactionAmount":{"amount":-120.00,"currency":"SEK"}},{"entryReference":"2017401730150711","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-21","transactionAmount":{"amount":-111.00,"currency":"SEK"}},{"entryReference":"2017401730150710","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-21","transactionAmount":{"amount":-65.43,"currency":"SEK"}},{"entryReference":"2017401730150709","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-21","transactionAmount":{"amount":-37.00,"currency":"SEK"}},{"entryReference":"2017401730150708","text":"Köp","merchantName":"merchantName","transactionDate":"2020-06-21","transactionAmount":{"amount":-37.00,"currency":"SEK"}}],"_links":{"first":{"href":"/openbanking/ais/v1/accounts/resourceId_3/transactions?&dateFrom=1991-03-10&bookingStatus=both"}}}}
