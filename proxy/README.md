# (Current) Proxy Solution

This readme contains documentation regarding the proxy solution for outgoing traffic.
The proxy is simply selecting the outgoing ip based on the userid and credentialsid.
We implemented it because some providers were blocking/throttling our ips. This solutions is effectively increasing throughtput.


## Proxy

The current setup is an nginx proxy using 2 modules. The `proxy_connect` and the `split_client`.

### proxy_connect

You can find the nginx module here: [proxy_connect](https://github.com/chobits/ngx_http_proxy_connect_module)

As described on the module documentation the client is doing an initial request (connect) to setup the connection and then forwards everything.

We use that connect request to pick the outgoing ip in the split_clients module.

### split_clients

You can find the nginx module here: [split_clients](http://nginx.org/en/docs/http/ngx_http_split_clients_module.html)

It's "advertised" as a tool that enables AB Testing.
Based on a criterion (a header for us), the module selects a value from the list.
We populated that list with outgoing ips bound to the machine, and then use that `$bind_ip` in the `proxy_connect` configuration to set ip.
The selection process is based on a value (a header for us) that is hashed, the same `userid+credentialsid` will always end up having the same outgoing ip.


## Configuration

The configuration looks like this today:

```

   split_clients "$http_proxy_authorization" $bind_ip {
     10% ACTUAL_IP_1;
     10% ACTUAL_IP_2;
     10% ACTUAL_IP_3;
     10% ACTUAL_IP_4;
     10% ACTUAL_IP_5;
     10% ACTUAL_IP_6;
     10% ACTUAL_IP_7;
     10% ACTUAL_IP_8;
     10% ACTUAL_IP_9;
     10% ACTUAL_IP_10;
     *  IP;
   }

   server {
       listen                         8080;

       # dns resolver used by forward proxying
       resolver                       8.8.8.8;

       # forward proxy for CONNECT request
       proxy_connect;
       proxy_connect_allow            443;
       proxy_connect_connect_timeout  10s;
       proxy_connect_read_timeout     10s;
       proxy_connect_send_timeout     10s;

       proxy_connect_bind $bind_ip;

       # forward proxy for non-CONNECT request
       location / {
           proxy_pass http://$host;
           proxy_set_header Host $host;
       }
   }
   ```

## Potential problems

The only known potential problem with the solution today is that if the machine restarts we need to ssh in and bind the ips to the interface.


You can do that like this:

```#!/bin/bash
set -e

macs=$(ip link | grep -o "link/ether .*" | cut -d ' ' -f2)
for mac in $macs; do
    ips=$(curl -s "http://169.254.169.254/latest/meta-data/network/interfaces/macs/$mac/local-ipv4s")
    for ip in $ips; do
        echo "IP: $ip"
        #sudo ip addr add $ip/20 dev ens3 || true
    done
done

```

As long as the outgoing IPs haven't changes, the nginx configuration should remain the same.
If the ips change, you might want to manually replace the IPs from the nginx configuration or do something like this:
```
#!/bin/bash
   set -e
   echo 'split_clients "$http_proxy_authorization" $bind_ip {'
   for ip in $(ip addr show ens3 | grep -o "inet [0-9.]*" | cut -d ' ' -f2); do
       echo "  10% $ip;"
   done

```



## Nginx

Nginx is installed on the machine and the configuration lives in /etc/nginx/conf or something.


## ssh

You can access the http-proxy machine through ssh. You can find the network ip from the salt master like this:
`salt 'http-proxy*' network.ipaddrs`