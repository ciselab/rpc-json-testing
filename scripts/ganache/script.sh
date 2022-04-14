#!/bin/bash

# Reset coverage
cd /ganache-cli
npm run start_clean > temp.txt &
while [ "$(wc -l < temp.txt)" -lt "46" ]; do
 echo "waiting for server to have started... $(lsof -t -i:8545 -sTCP:LISTEN)"
 sleep 0.1
done
# Kill the process
id=$(lsof -t -i:8545 -sTCP:LISTEN)
echo "killing: $id"
kill "$id"
while [ "$(lsof -t -i:8545 -sTCP:LISTEN)" != "" ]; do
 echo "waiting at kill...  -$(lsof -t -i:8545 -sTCP:LISTEN)-"
 sleep 0.1
done

# Run the tool
cd /blockchain-testing
# The first argument is the fitness function used (1), the second arg is the time the experiment will run (in mins) and the third arg is the server used (g or r). The fourth argument is the proportion of individuals to be mutated.
java -jar target/blockchain-testing-1.0-SNAPSHOT-jar-with-dependencies.jar $1 $2 $3 $4

# Kill the ganache server
id=$(lsof -t -i:8545 -sTCP:LISTEN)
echo "killing: $id"
kill "$id"
while [ "$(lsof -t -i:8545 -sTCP:LISTEN)" != "" ]; do
 echo "waiting at kill...  -$(lsof -t -i:8545 -sTCP:LISTEN)-"
 sleep 0.1
done

# Compute test coverage achieved by running the tool
cd /ganache-cli
nyc report > final_coverage_total.txt
echo "These results are based on server: " $3", fitness function:" $1 ", mutation proportion: " $4 " and time:" $2 "minutes." >> final_coverage_total.txt cat final_coverage_total.txt

# Reset coverage before running the tests
cd /ganache-cli
npm run start_clean > temp.txt &
while [ "$(wc -l < temp.txt)" -lt "46" ]; do
 echo "waiting for server to have started... $(lsof -t -i:8545 -sTCP:LISTEN)"
 sleep 0.1
done

# Kill the process
id=$(lsof -t -i:8545 -sTCP:LISTEN)
echo "killing: $id"
kill "$id"
while [ "$(lsof -t -i:8545 -sTCP:LISTEN)" != "" ]; do
 echo "waiting at kill...  -$(lsof -t -i:8545 -sTCP:LISTEN)-"
 sleep 0.1
done

# Run the generated tests
cd /blockchain-testing
mvn clean test -Dtest=generated.ind*
# Kill the process
id=$(lsof -t -i:8545 -sTCP:LISTEN) # Without the LISTEN part the Java process will be killed as well
echo "killing: $id"
kill "$id"
while [ "$(lsof -t -i:8545 -sTCP:LISTEN)" != "" ]; do
 echo "waiting at kill...  -$(lsof -t -i:8545 -sTCP:LISTEN)-"
 sleep 0.1
done

# Compute test coverage achieved by running the generated tests
cd /ganache-cli
nyc report > final_coverage_archive.txt
echo "These archive tests were based on server: " $3", fitness function:" $1 ", mutation proportion: " $4 " and time:" $2 "minutes." >> final_coverage_archive.txt cat final_coverage_archive.txt

exit