#!/bin/bash

# reset coverage
cd /rippled-1.6.0
find . -type f -name "*.gcda" -delete

# run the tool
cd /blockchain-testing
# the first argument is the fitness function used (1-8), the second arg is the time the experiment will run (in mins) and the third arg is the server used (g or r). The fourth argument is the proportion of individuals to be mutated.
java -jar target/blockchain-testing-1.0-SNAPSHOT-jar-with-dependencies.jar $1 $2 $3 $4

# kill current rippled server at port 5005
id=$(lsof -t -i:5005 -sTCP:LISTEN)
echo "killing: $id"
kill "$id"
while [ "$(lsof -t -i:5005 -sTCP:LISTEN)" != "" ]; do
 echo "waiting at kill...  -$(lsof -t -i:5005 -sTCP:LISTEN)-"
 sleep 0.1
done

# compute test coverage achieved by running the tool
cd /rippled-1.6.0
gcovr -s -b -r ./ -f 'src/ripple' -o final_bcoverage_total.txt
echo "These results are based on server: " $3", fitness function:" $1 ", and time:" $2 "minutes." >> final_bcoverage_total.txt cat final_bcoverage_total.txt
gcovr -s -r ./ -f 'src/ripple' -o final_lcoverage_total.txt
echo "These results are based on server: " $3", fitness function:" $1 ", and time:" $2 "minutes." >> final_lcoverage_total.txt cat final_lcoverage_total.txt

# reset coverage and run the generated tests
find . -type f -name "*.gcda" -delete 
cd /blockchain-testing
mvn clean test -Dtest=generated.ind*

# kill current rippled server at port 5005
id=$(lsof -t -i:5005 -sTCP:LISTEN)
echo "killing: $id"
kill "$id"
while [ "$(lsof -t -i:5005 -sTCP:LISTEN)" != "" ]; do
 echo "waiting at kill...  -$(lsof -t -i:5005 -sTCP:LISTEN)-"
 sleep 0.1
done

# compute test coverage achieved by running the generated tests
cd /rippled-1.6.0
gcovr -s -b -r ./ -f 'src/ripple' -o final_bcoverage_archive.txt
echo "These archive tests were based on server: " $3", fitness function:" $1 ", and time:" $2 "minutes." >> final_bcoverage_archive.txt cat final_bcoverage_archive.txt
gcovr -s -r ./ -f 'src/ripple' -o final_lcoverage_archive.txt
echo "These archive tests were based on server: " $3", fitness function:" $1 ", and time:" $2 "minutes." >> final_lcoverage_archive.txt cat final_lcoverage_archive.txt

exit
