#!/bin/bash


# kill current rippled server at port 5005
id=$(lsof -t -i:5005 -sTCP:LISTEN) # without the LISTEN part the Java process will be killed as well

echo "killing: $id"
kill "$id"

while [ "$(lsof -t -i:5005 -sTCP:LISTEN)" != "" ]; do
 echo "waiting at kill...  -$(lsof -t -i:5005 -sTCP:LISTEN)-"
 sleep 0.1
done

echo "computing coverage"
cd rippled-1.6.0
gcovr -s -r ./

exit 0