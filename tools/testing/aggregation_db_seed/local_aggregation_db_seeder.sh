##################################################################
#   Script to seed aggregation with local testing configurations #
##################################################################
aggregation_pod=$( kubectl get --no-headers=true pods --selector=app=aggregationdb -o name | awk -F "/" '{print $2}')
kubectl exec -it  $aggregation_pod mysql <./script.sql