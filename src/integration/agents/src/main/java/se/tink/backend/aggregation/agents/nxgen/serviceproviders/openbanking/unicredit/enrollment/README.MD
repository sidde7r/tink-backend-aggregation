Unicredit enrollment

* login using aws-ssh script to runner
* create enrol-pod.yaml there
* run ```kubectl create -f enrol-pod.yaml```
* access pod ```kubectl exec -it tmp-shell-enrol bash -n eidas-proxy```
* execute script with proper appId and email
* exit pod ```kubectl delete po tmp-shell-enrol -n eidas-proxy```
