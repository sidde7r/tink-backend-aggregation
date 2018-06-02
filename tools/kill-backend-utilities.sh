#!/bin/bash

kill $(pgrep nginx)
kill $(pgrep memcached)
kill $(pgrep mysql)
kill `pgrep -f elasticsearch-0`
