# kill current rippled server at port 5005
kill $(lsof -t -i:5005 -sTCP:LISTEN) # THE LISTEN PART IS VERY IMPORTANT BECAUSE OTHERWISE YOU WILL ALSO KILL THE JAVA PROCESS!!
sleep 0.5

# start rippled server again
cd /rippled-1.6.0/build/cmake/coverage
./rippled -a --start -v --debug &
sleep 0.5

exit 0
