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
./setup-minikube.sh --cert-manager
```

From the tink-backend-aggregation root do:

Set up aggregation-db:
```bash
kubectl create -f ./kubernetes/development/aggregationdb.yml
```

Set up cache:

```bash
kubectl create -f ./kubernetes/development/cache.yml
```

Set up coordination:
```bash
kubectl create -f ./kubernetes/development/coordination.yml
```

If you intend to run the provider configuration service, setup the providerdb:
```bash
kubectl create -f ./kubernetes/development/providerdb.yml
```

If you run into problems try deleting both the service and the deployment:
```bash
kubectl delete deployment.apps/coordination
kubectl delete deployment.apps/cache
kubectl delete deployment.apps/aggregationdb
kubectl delete service -n default coordination
kubectl delete service -n default cache
kubectl delete service -n default aggregationdb
```

## Running aggregation locally

```bash
bazel run //src/aggregation/service:bin
```

or if you are using intelliJ you can also use the available run configuration "Minikube-Server: Aggregation"

## Deploy and run aggregation using kubernetes

### Prerequisite
When running locally, the docker image is not downloaded by kubernetes setup, as it is for other environments.

To do that run:
```bash
docker pull gcr.io/tink-containers/debian:stretch
```

Because of missing image, following error can occur:

```bash
$ kubectl get pods -n aggregation
NAME                           READY   STATUS                   RESTARTS   AGE
aggregation-84d6b49dbf-fqfpt   0/1     Init:ErrImageNeverPull   0          6s
```
You can also see an error in the events when describing the pods:

```bash
$ kubectl -n aggregation describe pod <pod>
Events:
  Type     Reason             Age                    From               Message
  ----     ------             ----                   ----               -------
  Warning  FailedScheduling   45m (x2 over 45m)      default-scheduler  0/1 nodes are available: 1 node(s) didn't match pod affinity/anti-affinity, 1 node(s) didn't satisfy existing pods anti-affinity rules.
  Normal   Scheduled          45m                    default-scheduler  Successfully assigned aggregation/<pod> to minikube
  Warning  ErrImageNeverPull  7m56s (x139 over 45m)  kubelet, minikube  Container image "gcr.io/tink-containers/debian:stretch" is not present with pull policy of Never
  Warning  Failed             2m50s (x163 over 45m)  kubelet, minikube  Error: ErrImageNeverPull
```

Also ensure that the vault chart has been correctly applied, we need it to create the secrets that are
necessary for the aggregation pod to start. Do this by running the following command from the root of
the tink-infrastructure repository:
```bash
$ .charts/vault/local-development.sh
```

This command will take some time, and the entire process isn't *strictly* necessary. As soon as the command 
`kubectl -n vault get secret/internal-ca` returns successfully, the `local-development.sh` command can be aborted. 
This might take some time however. Do check the `jobs` and `pods` inside the vault namespace to see if there 
are any problems regarding cpu-resources or authentication towards Google Cloud.

After vault has been successfully provisioned, we now have to copy them to the aggregation namespace, so that 
kubernetes can read them and create the our specific client-certificate. Do this by running.
```bash
$ kubectl -n vault get secret/internal-ca --export -o yaml | kubectl -n aggregation apply -f-
secret/internal-ca configured
```

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
<your_tink_infrastructure_directory>/kubernetes-generator/kubernetes-generator.sh \
    --cluster local --environment development \
    --chart tink-backend-aggregation \
    --repo <tink_backend_aggregation_directory> | kubectl apply -f-
```
Do note, that this will also create the local infrastructure components as well, much like how we were able to do above.  
Verify that the pods are up and healthy:
```bash
kubectl -n aggregation get pods
```

Done!


### Or as a one liner:
```bash
eval $(minikube docker-env) && bazel run //docker:aggregation_image && \
    <your_tink_infrastructure_directory>/kubernetes-generator/kubernetes-generator.sh \
    --chart tink-backend-aggregation \
    --cluster local --environment development | kubectl apply -f -
```


