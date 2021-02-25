REQUEST 1
POST https://openapi.kbc-group.com/ASK/oauth/token/1
Authorization: Basic RFVNTVlfSUQ=
Content-Type: application/x-www-form-urlencoded
PSU-IP-Address: 0.0.0.0

refresh_token=refreshToken&grant_type=refresh_token&client_id=DUMMY_ID

RESPONSE 1
200
Content-Type: application/json;charset=utf-8
Server: KBCGroupNV

{"access_token":"accessToken","expires_in":"900","token_type":"Bearer","refresh_token":"DUMMY_REFRESH_TOKEN","scope":"AIS"}

REQUEST 2
GET https://psd2.api.kbc.be/psd2/v2/accounts?withBalance=true
Authorization: Bearer accessToken
Consent-ID: dummy_consent_id
PSU-IP-Address: 0.0.0.0

RESPONSE 2
401
Content-Type: application/json;charset=utf-8
Server: KBCGroupNV

{"tppMessages":[{"category":"ERROR","code":"CONSENT_EXPIRED"}]}
