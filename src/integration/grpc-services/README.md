# Grpc Services
aka Transport layer


## Query example

running locally:

`grpcurl -plaintext -d '{"message": "123"}' -proto proto/integration_service/services.proto localhost:8443 PingService.Ping`

running locally on minikube:

`grpcurl -plaintext -d '{"message": "123"}' -proto proto/integration_service/services.proto 192.168.99.100:31011 PingService.Ping`


## Tools

|  name |  purpose |
|---|---|
|[grpcurl](https://github.com/fullstorydev/grpcurl#installation)| Run curl like queries on your grpc server  |
