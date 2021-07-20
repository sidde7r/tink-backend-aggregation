# Polish API Service Provider

## Background
Openbanking APIs for Banks in Poland are based on Polish API.

Documentation: https://polishapi.org/wp-content/uploads/2019/12/PolishAPI-specification-v3.0.pdf
Swagger: https://app.swaggerhub.com/apis/ZBP/polish-api/3_0

Polish API returns data about Checking, Savings and Credit Cards from one endpoint.

Not all banks uses API in v3 version - but for AIS there are no differences.

## Differences between banks

Polish API is a standard but banks may have a different approach in terms of:
 1. Having API which calls endpoints either with a GET, or a POST request type. Currently, only mBank uses Get flow.
 2. Consent flow - GET API has different consent types comparing to POST API. 
    Also, some banks support exchange token flow and some not. 
 3. Signing requests - some banks requires signing headers and some not.
 4. Different headers and some specific field values - this is handled by redundancy of information which is sent
    e.g. ```X-JWS-SIGNATURE``` and ```JWS-SIGNATURE``` are sent.
 5. Addresses fields are handled differently.
 6. Redirect URIs may be fetched from different fields.
 7. List of account numbers might be fetched from the Token response.
 
## How to implement agent using this service provider?

Add an agent which extends PolishApiAgent - you will need to implement functions from
```PolishApiAgentCreator```. Interface is documented with Java Docs which describes what should be set.

If you see that your bank behaves differently in some particular way - extend the interface and
add support within PolishApi service-provider.

In case of any hesitations ask bank support / PO for information.

If dynamic registration is supported - add proper code to tppregistration in tink-backend.

Please verify how bank behaves after 30 minutes after creating consent. Some banks do not
allow fetching data longer than 90 days anymore after first authentication.

## Consent flow
Consent flow - in almost all cases (currently apart from PKO) - all banks support exchange token flow.

How it works?
In the first request with scope ais-accounts consent is sent. After we receive token
we need to exchange that token for scope ais. This operation is not reversible - this is
why we store accounts in persistent storage.

There is two types of consents SINGLE and MULTIPLE. Currently, we sent MULTIPLE everywhere,
that means we can later fetch data.

In the consent we can pass information about how big transaction history we want to fetch. 
Currently this is max possible amount.

## Logging
Extensive logging is done in most crucial parts of the agent:
 1. Authentication,
 2. Getting data from persistent storage,
 3. Fetching transactions.
 
## Signature
Every request is signed in PolishApiSignatureFilter. For that case utility class PolishApiJwsSignatureProvider is created.
Requests to the polish banks need to have JWSHeader sent with the following information:
 1. x509CertURL (URL to the endpoint with PEM certificate) - aka x5u
 2. x509CertSHA256Thumbprint (thumbprint of encoded root certificate in DER format) - aka x5t256.

Polish API uses RS256 as signing algorithm. Requests are signed using QSealC cert.

Signature is in detached format - which means that payload in JWS is omitted 
(see RFC7515 https://datatracker.ietf.org/doc/html/rfc7515 as reference).

For polish banks new CAs were added to Eidas Proxy:
* https://github.com/tink-ab/tink-backend/pull/34997 (NC)
  * https://www.nccert.pl/files/nccert2016.crt 
  * (valid until: Saturday, 10 December 2039 at 00:59:59 Central European Standard Time)
* https://github.com/tink-ab/tink-backend/pull/35008 (CA Szafir - qualified) 
  * http://elektronicznypodpis.pl/certyfikaty/ozk62.der 
  * (valid until: Tuesday, 14 March 2028 at 00:59:59 Central European Standard Time)


## Error handling
Error handling is done in errorhandling package. All cases apart from expired token are handled in:
```PolishApiErrorHandler```.

If there is unhandled error case you will see below information in the logs:
```[Polish API] Error handler - Unhandled issue - please add handling!```

There might be the case that token got expired during the data fetch - for that case ```PolishApiRefreshTokenFilter``` is created.

## Other concerns
1. Please note - that some entities are used both for Requests and Responses. Different values might be needed depending on
the request / response. e.g. ```RequestHeaderEntity```. Currently, redundant data might be sent, but it does not appear to be an issue.
2. Account mapping is done basing on patterns. So you need to provide list of patterns in defined AccountTypeMapper.  
3. ApiClient for fetching accounts stores accounts in the persistent storage which means that if
accounts where fetched successfully once - all next accounts will be returned from persistent storage.
4. mBank uses internal account identifier as param for account details / transactions while all other
banks uses accountNumber.
5. mBank seems to return corporate accounts in the same API (hence filtering is applied - that might be confusing for the users)
6. Some banks do not like unnecessary parameters. For that case you need to provide a list of fields that needs
to be filtered. Filtering is done in ```PolishApiRedundantFieldsFilter```.

## Issues / TODOs
 * API does not return information about credit card number (it returns iban) and available credit, hence as number IBAN is passed and available credit is set to 0.
 * Agent does not handle information about bank being down.
 * Not all errors might be handled correctly.
 * Missing tests
 * Missing translations
 * No support for corporate accounts.
 * Currently, only DONE and PENDING transaction are handled (even in consents) if you want to
 extend that behaviour new consents must be passed.
 * MULTIPLE consent is sent everytime
 * In consent, we always sent maximum possible value for transaction history and sets consent length to 90 days. 
 * There is a lot of unmapped transaction fields which might be useful
 * We are not verifying (X-)JWS-Signature returned from bank
 * Account mapping is missing.
 * Add alerting for unmapped accounts
 * Pagination for accounts fetch is not done, but we can obtain 100 accounts in one page so that is not a big issue.

## More references
Polish API comparision (mBank, Alior, PKO BP):
https://docs.google.com/document/d/18ggA1sCwdzqt2UxMPpfhoCIOCzYdR6YPuw2TZAe7-5g/edit?usp=drive_web&ouid=116356725447615726705

Polish API comparision (data points, max history length, supported endpoints):
https://docs.google.com/spreadsheets/d/19UeuPQ7Qnjk5JoZ4bVMy5budLxThxOgk-owMnMyBcv0/edit
