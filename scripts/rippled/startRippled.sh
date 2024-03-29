#!/bin/bash

# kill current rippled server at port 5005
id=$(lsof -t -i:5005 -sTCP:LISTEN) # without the LISTEN part the Java process will be killed as well

echo "killing: $id"
kill "$id"

# kill previous session server if it is still running
count=0
while [ "$(lsof -t -i:5005 -sTCP:LISTEN)" != "" ]; do
 echo "waiting at kill...  -$(lsof -t -i:5005 -sTCP:LISTEN)-"
 if [[ $count == 1000 ]]; then
   id=$(lsof -t -i:5005 -sTCP:LISTEN)
   echo "killing: $id"
   kill "$id"
   count=0
 fi
 count=$((count+1))
 sleep 0.1
done

# delete db files
rm -rf /var/lib/rippled/db

echo "starting server at port 5005"
# start rippled server again
cd /rippled-1.6.0/build/cmake/coverage
./rippled -a --start -v --debug &

# wait until server has started
count=0
while [ "$(lsof -t -i:5005 -sTCP:LISTEN)" == "" ]; do
 echo "waiting at server... $(lsof -t -i:5005 -sTCP:LISTEN)"
 if [[ $count == 1000 ]]; then
    ./rippled -a --start -v --debug &
    count=0
 fi
 count=$((count+1))
 sleep 0.1
done

exit 0