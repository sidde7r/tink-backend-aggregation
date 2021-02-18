REQUEST 1
GET https://openapi.kbc-group.com/psd2/v2/accounts?withBalance=true
Authorization: Bearer accessToken
Consent-ID: dummy_consent_id
PSU-IP-Address: 0.0.0.0

RESPONSE 1
401
Content-Type: application/json;charset=utf-8
Server: KBCGroupNV

{"tppMessages":[{"category":"ERROR","code":"CONSENT_EXPIRED"}]}
