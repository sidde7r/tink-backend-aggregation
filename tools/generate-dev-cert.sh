#!/bin/bash

ED=$HOME/.eidas
CLIENTKEY="$ED/client.key"
mkdir -p "$ED"

if test -f "$CLIENTKEY"; then
    read -p "$CLIENTKEY already exists, would you like to overwrite? " -n 1 -r
    if [[ $REPLY =~ ^[Yy]$ ]]
    then
        rm "$CLIENTKEY"
    else
        echo "cancelled"
        exit 1
    fi
fi

read -rp "Enter your username [$USER]: " name
name=${name:-$USER}

read -rp "Enter your email [$name@tink.se]: " email
email=${email:-$name@tink.se}

echo "Generating certificate with subject /CN=$name/emailAddress=$email"
openssl req -new -keyout "$CLIENTKEY" -out "$HOME/$name.csr"\
  -nodes -subj "/CN=$name/emailAddress=$email" -newkey rsa:4096

echo "Your CSR has been saved at $HOME/$name.csr"

