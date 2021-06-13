# kill current rippled server at port 5005
kill $(lsof -t -i:5005)
sleep 1

# start rippled server again
cd /rippled-1.6.0/build/cmake/coverage
./rippled -a --start -v --debug & disown
sleep 1

exit 0
