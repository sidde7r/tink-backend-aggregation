#!/bin/bash
cat test_output.txt|grep '"id":'|grep transactions|sed 's/^.* \[main\] DEBUG org.apache.http.wire -  << "//g'|sed 's/"$//' > test_transactions.txt
