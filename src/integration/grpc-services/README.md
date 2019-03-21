# Grpc Services
aka Transport layer


## Query example

running locally:

`grpcurl -plaintext -d '{"message": "123"}' -proto proto/integration_service/services.proto localhost:8443 PingService.Ping`

running locally on minikube:

`grpcurl -plaintext -d '{"message": "123"}' -proto proto/integration_service/services.proto 192.168.99.100:31011 PingService.Ping`


Fetch request:
`grpcurl -plaintext -d '{"operationId": "123", "agentInfo": {"agentClassName":"nxgen.demo.banks.password.PasswordDemoAgent", "state": {"state":"1234"}}, "aggregatorInfo": {"clientId": "client-id-1234", "aggregatorIdentifier": "Tink"}, "credentials": {"id": "credentialsId-1234", "userId": "userId-1234", "fieldsSerialized":"", "type": "TYPE_PASSWORD"} }' -proto proto/integration_service/services.proto localhost:8443 FetchService.CheckingAccounts`



## Tools

|  name |  purpose |
|---|---|
|[grpcurl](https://github.com/fullstorydev/grpcurl#installation)| Run curl like queries on your grpc server  |
