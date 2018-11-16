1. First of all you need to set up kubernetes.

2. Start the infrastructure components
```
kubectl apply -f kubernetes/development
```

3. Build the container images
```
bazel build docker:bundle.tar
```

4. Load into the minikube docker daemon
```
eval $(minikube docker-env)
docker load -i bazel-bin/docker/bundle.tar
```

5. Run kubernetes generator to apply the charts
```
../tink-infrastructure/kubernetes-generator/kubernetes-generator.sh --chart tink-backend-provider-configuration | kubectl apply -f -
../tink-infrastructure/kubernetes-generator/kubernetes-generator.sh --chart tink-backend-aggregation | kubectl apply -f -
```
