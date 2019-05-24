env=$1

if [ $# -eq 0 ]
  then
    echo "#############################################################################################################"
    echo "If you are running with a private Docker registry, please run ./RunAggregation.sh <yourPrivateRegistry>:5000"
    echo "#############################################################################################################"
else
    endpoint=$env
fi

bazel run //src/aggregation/service:aggregation_debug_image

if [ $# -ne 0 ]
  then
    echo "#############################################################################################################"
    echo "Remember to change your image in .charts/tink-backend-aggregation/values/local-development to  $env/aggregation/service:aggregation_debug_image"
    echo "#############################################################################################################"
    docker tag bazel/src/aggregation/service:aggregation_debug_image $env/aggregation/service:aggregation_debug_image
    docker push $env/aggregation/service:aggregation_debug_image
fi

kubectl delete deployment -n aggregation aggregation
../tink-infrastructure/kubernetes-generator/kubernetes-generator.sh --chart tink-backend-aggregation --cluster local --environment development | kubectl apply -f-
