#!/bin/bash


# kill current ganache server at port 8545
id=$(lsof -t -i:8545 -sTCP:LISTEN) # Without the LISTEN part the Java process will be killed as well

echo "killing: $id"
kill "$id"

while [ "$(lsof -t -i:8545 -sTCP:LISTEN)" != "" ]; do
 echo "waiting at kill...  -$(lsof -t -i:8545 -sTCP:LISTEN)-"
 sleep 0.1
done

echo "computing coverage"
cd /ganache-cli
nyc report --reporter=lcov --reporter=text-summary

exit 0