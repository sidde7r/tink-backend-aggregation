#!/bin/bash
set -e

ED=$HOME/.eidas
CLIENTKEY="$ED/client.key"

if ! test -f "$1"; then
    echo "Certificate not found"
    exit 1
fi

if test -f "$CLIENTKEY"; then
    openssl pkcs12 -export -clcerts -in "$1" -inkey "$CLIENTKEY" -out "$HOME/eidas_client.p12" -passout pass:changeme
    echo "Your client certificate has been saved to $HOME/eidas_client.p12"
else
    echo "client key not found"
fi
