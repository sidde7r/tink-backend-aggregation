# Aggregation Service

The responsibility of the aggregation service is to aggregate data 
incoming from our bank connectors, i.e. agents.
It communicates to the rest of Tink's ecosystem via the aggregation 
controller.

The function of the aggregation api is to communicate with external
 entities (e.g. the banks) in a unified way.


## Setting up infrastructure components
To be able to start aggregation, you also need the database `aggregation-db`, `coordination` (zookeper) and `cache`.

These can be started from the tink-infrastructure repo:

```bash
./setup-minikube --cert-manager
```

Set up aggregation-db:
```bash
kubectl create -f kubernetes/development/aggregationdb.yml
```

Set up cache:

```bash
kubectl kubectl create -f kubernetes/development/cache.yml
```

Set up coordination:
```bash
kubectl create -f kubernetes/development/coordination.yml
```

## Running aggregation locally 

```bash
bazel run //src/aggregation/service:bin server etc/development-minikube-aggregation-server.yml
```

or if you are using intelliJ you can also use the available run configuration "Minikube-Server: Aggregation"

## Deploy and run aggregation using kubernetes

### Step by step guide

If you are using minikube, connect docker to the docker server running inside minikube:
```bash
eval $(minikube docker-env)
```
Build the image:
```bash
bazel run //docker:aggregation_image
```

Deploy aggregation to your local kubernetes cluster:
```bash
<your_tink_infrastructure_directory>/kubernetes-generator/kubernetes-generator.sh --cluster local --environment development --chart tink-backend-aggregation --repo <tink_backend_aggregation_directory> | kubectl apply -f-
```

Verify that the pods are up and healthy:
```bash
kubectl -n aggregation get pods 
```

Done!


### Or as a one liner:
```bash
eval $(minikube docker-env) && bazel run //docker:aggregation_image && <your_tink_infrastructure_directory>/kubernetes-generator/kubernetes-generator.sh --chart tink-backend-aggregation --cluster local --environment development | kubectl apply -f -
```


