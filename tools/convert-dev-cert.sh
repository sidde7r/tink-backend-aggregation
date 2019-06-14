#!/bin/bash

openssl pkcs12 -export -clcerts -in $1 -inkey $2 -out eidas_client.p12 -passout pass:changeme
