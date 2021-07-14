#!/bin/bash


# kill current ganache server at port 8545
id=$(lsof -t -i:8545 -sTCP:LISTEN) # Without the LISTEN part the Java process will be killed as well

echo "killing: $id"
kill "$id"

while [ "$(lsof -t -i:8545 -sTCP:LISTEN)" != "" ]; do
 echo "waiting at kill...  -$(lsof -t -i:8545 -sTCP:LISTEN)-"
 sleep 0.1
done

echo "starting server at port 8545"
# start ganache server again
cd /ganache-cli
rm -f output.txt
> output.txt
npm run start > output.txt &

while [ "$(wc -l < output.txt)" -lt "46" ]; do
 echo "waiting at server... $(lsof -t -i:8545 -sTCP:LISTEN)"
 sleep 0.1
done

exit 0