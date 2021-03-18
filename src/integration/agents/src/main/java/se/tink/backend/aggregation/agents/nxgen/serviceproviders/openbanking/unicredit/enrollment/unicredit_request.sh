alias uuidgen="cat /proc/sys/kernel/random/uuid"
email=
appId=
clusterId=oxford-production
curl -d "{\"userEmail\": \"$email\"}" https://api.unicredit.eu/tpp/v1/authentications/ \
      --proxy-cert /etc/client-certificate/tls.crt \
      --proxy-key /etc/client-certificate/tls.key \
      --proxy-cert-type pem \
      --proxy-cacert /etc/client-certificate/ca.crt \
      --proxy-insecure \
      --proxy 'https://tink-integration-eidas-proxy.eidas-proxy.svc.cluster.local:9022' \
      --cert /etc/client-certificate/tls.crt \
      --key /etc/client-certificate/tls.key \
      --cert-type pem \
      --insecure --verbose \
      -H "X-Request-ID: $(uuidgen)" \
      -H "Content-Type: application/json" \
      -H "X-Tink-QWAC-appId: $appId" \
      -H "X-Tink-QWAC-ClusterId: $clusterId"
