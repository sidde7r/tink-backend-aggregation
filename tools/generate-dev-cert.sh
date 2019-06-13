#!/bin/bash

read -p "Enter your username [$USER]: " name
name=${name:-$USER}
echo $name

read -p "Enter your email [$USER@tink.se]: " email
email=${email:-$USER@tink.se}
echo $email

openssl req -new -keyout $name.key -out $name.csr\
  -nodes -subj "/CN=$name/emailAddress=$email" -newkey rsa:4096


