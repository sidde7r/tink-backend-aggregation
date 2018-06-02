#Require grpcc, see https://github.com/njpatel/grpcc

echo "==== Ping Leeds staging ==="
grpcc --proto proto/services.proto --address api-grpc.staging.abnamro.tinkapp.nl:443 -s PingService --eval 'client.ping({message: "pong"}, pr);'

echo "==== Ping Leeds production ==="
grpcc --proto proto/services.proto --address main-grpc.production.abnamro.tinkapp.nl:443 -s PingService --eval 'client.ping({message: "pong"}, pr);'

echo "==== Ping Oxford staging ==="
grpcc --proto proto/services.proto --address staging-grpc.oxford.tink.se:443 -s PingService --eval 'client.ping({message: "pong"}, pr);'

echo "==== Ping Oxford production ==="
grpcc --proto proto/services.proto --address main-grpc.production.oxford.tink.se:443 -s PingService --eval 'client.ping({message: "pong"}, pr);'
