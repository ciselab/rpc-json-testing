#!/bin/bash

# reset coverage
cd rippled-1.6.0
find . -type f -name "*.gcda" -delete
cd ..

# run the tool
cd blockchain-testing
# the first argument is the fitness function used (1-8), the second arg is the time the experiment will run (in mins) and the third arg is the server used (g or r).
java -jar target/blockchain-testing-1.0-SNAPSHOT-jar-with-dependencies.jar $1 $2 $3
cd /

# kill current rippled server at port 5005
id=$(lsof -t -i:5005 -sTCP:LISTEN)
echo "killing: $id"
kill "$id"
while [ "$(lsof -t -i:5005 -sTCP:LISTEN)" != "" ]; do
 echo "waiting at kill...  -$(lsof -t -i:5005 -sTCP:LISTEN)-"
 sleep 0.1
done

# compute test coverage achieved by running the tool
cd rippled-1.6.0
gcovr -s -b -r ./ -o final_coverage_total.txt
echo "This run was based on server: " $3", fitness function:" $1 ", and time:" $2 "minutes." >> final_coverage_total.txt cat final_coverage_total.txt

# reset coverage and run the generated tests
find . -type f -name "*.gcda" -delete 
cd /
cd blockchain-testing
mvn clean test -Dtest=generated.ind*
cd /

# kill current rippled server at port 5005
id=$(lsof -t -i:5005 -sTCP:LISTEN)
echo "killing: $id"
kill "$id"
while [ "$(lsof -t -i:5005 -sTCP:LISTEN)" != "" ]; do
 echo "waiting at kill...  -$(lsof -t -i:5005 -sTCP:LISTEN)-"
 sleep 0.1
done

# compute test coverage achieved by running the generated tests
cd rippled-1.6.0
gcovr -s -b -r ./ -o final_coverage_archive.txt
echo "These archive tests were based on server: " $3", fitness function:" $1 ", and time:" $2 "minutes." >> final_coverage_archive.txt cat final_coverage_archive.txt

exit
