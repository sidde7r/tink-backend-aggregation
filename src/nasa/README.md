# NASA - Notifying Aggregation Service Asserter

What is this service used for?

## Running NASA locally with bazel:
1. In the terminal run: `bazel run //src/nasa/service:nasa etc/nasa/development-configuration.yaml`

## Running NASA locally with Kubernetes:
1. In the terminal run: `eval $(minikube docker-env)` (https://stackoverflow.com/a/42564211)
2. In the same terminal window run: `bazel run //src/nasa/service:docker`
3. From `tink-backend-aggregation` run: `../tink-infrastructure/kubernetes-generator/kubernetes-generator.sh --cluster local --environment development --chart tink-backend-notifying-aggregation-service-asserter --repo /Users/larsalmgren/src/tink-backend-aggregation/ | kubectl apply -f-`

If you do any updates to the Java code you’ll need to build the image again (step two).
To get the latest image running in Kubernets you can do,
`kubectl -n notifying-aggregation-service-asserter delete pod <pod name>`.
This will delete the current running pod and the new pod that (should) start up will run the newly built image.

If you do any updates to the charts you will need to do step three again.
Before you actually run step three it could be necessary to delete the deployment,
`kubectl -n notifying-aggregation-service-asserter delete deployment notifying-aggregation-service-asserter`.
