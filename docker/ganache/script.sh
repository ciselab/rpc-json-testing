#!/bin/bash

# (!!!) reset coverage is not being done

# run the tool
cd blockchain-testing
# the first argument is the fitness function used (1-8), the second arg is the time the experiment will run (in mins) and the third arg is the server used (g or r).
java -jar target/blockchain-testing-1.0-SNAPSHOT-jar-with-dependencies.jar $1 $2 $3
cd ..

# kill the process
id=$(lsof -t -i:8545 -sTCP:LISTEN)
echo "killing: $id"
kill "$id"
while [ "$(lsof -t -i:8545 -sTCP:LISTEN)" != "" ]; do
 echo "waiting at kill...  -$(lsof -t -i:8545 -sTCP:LISTEN)-"
 sleep 0.1
done

# compute test coverage achieved by running the tool
cd ganache-cli
# reset coverage?
nyc report --reporter=lcov --reporter=text
echo "These results are based on server: " $3", fitness function:" $1 ", and time:" $2 "minutes." >> final_coverage_total.txt cat final_coverage_total.txt
cd ..

# reset coverage and run the generated tests
# (!!!) reset coverage is not being done
cd blockchain-testing
mvn clean test -Dtest=generated.ind*

# kill the process
id=$(lsof -t -i:8545 -sTCP:LISTEN) # Without the LISTEN part the Java process will be killed as well
echo "killing: $id"
kill "$id"
while [ "$(lsof -t -i:8545 -sTCP:LISTEN)" != "" ]; do
 echo "waiting at kill...  -$(lsof -t -i:8545 -sTCP:LISTEN)-"
 sleep 0.1
done

# compute test coverage achieved by running the generated tests
cd ganache-cli
nyc report --reporter=lcov --reporter=text
echo "These archive tests were based on server: " $3", fitness function:" $1 ", and time:" $2 "minutes." >> final_coverage_archive.txt cat final_coverage_archive.txt

exit