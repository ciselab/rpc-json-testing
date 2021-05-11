#!/bin/bash

# run the rippled server in the background
cd rippled-1.6.0/build/cmake/coverage
./rippled -a -v --debug & disown
sleep 180
cd ../../../../

# run the tool
cd blockchain-testing
# the first argument indicates the fitness function used (1-7) and the second the time the experiment will run (in ms).
java -jar target/blockchain-testing-1.0-SNAPSHOT-jar-with-dependencies.jar $1 $2
cd ..

# delete generated cov files and restart rippled server to run tests
kill $(lsof -t -i:5005)
sleep 120
cd rippled-1.6.0
find . -type f -name "*.gcda" -delete 
cd build/cmake/coverage
./rippled -a -v --debug & disown
sleep 180
cd ../../../../
cd blockchain-testing
mvn clean test -Dtest=generated.ind*

# calculate test coverage
kill $(lsof -t -i:5005)
sleep 120
cd ..
cd rippled-1.6.0
gcovr -b -r ./ -o coverage_results.txt
echo "Fitness:" $1 "and time:" $2 "minutes" >> coverage_results.txt cat coverage_results.txt

exit