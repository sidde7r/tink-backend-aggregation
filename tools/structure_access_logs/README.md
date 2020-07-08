# Structure Agent Access logs

## Problem statement

The access logs produced by integration agents are quite tricky to for scripting and analysis because the parts related to the same operation are on multiple lines, with a relatively loose structure.

## STAL

This Go script takes a log file either from standard in or as it's first app argument and outputs in a structured JSON format with the following structure:
```
type Entry struct {
	ReqMethod  string            `json:"requestMethod"`
	ReqUrl     string            `json:"requestUrl"`
	ReqTime    time.Time         `json:"requestTime"`
	ReqHeaders map[string]string `json:"requestHeaders"`
	ReqBody    string            `json:"requestBody"`
	RspCode    int               `json:"responseCode"`
	RspTime    time.Time         `json:"responseTime"`
	RspHeaders map[string]string `json:"responseHeaders"`
	RspBody    string            `json:"responseBody"`
}
```

## Example usage

`cat my_access_log.log | stal`

## Example output
A log fine containing the following entry
```
1 * Client out-bound request
1 * 2020-07-08--07:00:11.525
1 > POST https://api.handelsbanken.com/bb/gls5/oauth2/token/1.0
1 > Accept: application/json
1 > Content-Type: application/x-www-form-urlencoded
1 > X-Aggregator: Tink
grant_type=client_credentials&scope=AIS&client_id=someuuid&psu_id_type=1
1 * Client in-bound response
1 * 2020-07-08--07:00:11.863
1 < 200
1 < Date: Wed, 08 Jul 2020 05:00:11 GMT
1 < Date: Wed, 08 Jul 2020 05:00:11 GMT
1 < X-Powered-By: Servlet/3.0
1 < Cache-Control: no-store
1 < Expires: 0
1 < Pragma: no-cache
1 < Content-Type: application/json;charset=utf-8
1 < Content-Language: en-US
1 < X-Pad: ***
1 < Content-Length: 112
1 < Server: Jetty(9.4.z-SNAPSHOT)
1 < 
{"access_token":"***MASKED***","expires_in":***MASKED***,"token_type":"***MASKED***"}
```
will produce produces a line with the following JSON data:
```
{"requestMethod":"POST","requestUrl":"https://api.handelsbanken.com/bb/gls5/oauth2/token/1.0","requestTime":"2020-07-08T07:00:11-52:50","requestHeaders":{"Accept":" application/json","Content-Type":" application/x-www-form-urlencoded","X-Aggregator":" Tink"},"requestBody":"grant_type=client_credentials&scope=AIS&client_id=someuuid&psu_id_type=1","responseCode":200,"responseTime":"2020-07-08T07:00:11-86:30","responseHeaders":{"Cache-Control":" no-store","Content-Language":" en-US","Content-Length":" 112","Content-Type":" application/json;charset=utf-8","Date":" Wed, 08 Jul 2020 05","Expires":" 0","Pragma":" no-cache","Server":" Jetty(9.4.z-SNAPSHOT)","X-Pad":" ***","X-Powered-By":" Servlet/3.0"},"responseBody":"{\"access_token\":\"***MASKED***\",\"expires_in\":***MASKED***,\"token_type\":\"***MASKED***\"}"}
```
