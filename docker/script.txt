# install required packages
apt-get update
apt-get install -y --no-install-recommends lsof procps

# run the rippled server in the background
cd rippled-1.6.0/build/cmake/coverage
./rippled -a -v --debug & disown
cd ../../../../

# run the tool
cd blockchain-testing
git pull
mvn clean install -DskipTests
cp src/main/resources/ripple-openrpc.json target/
# the first argument indicates the fitness function used (1-7) and the second the time the experiment will run (in ms).
#java -jar target/blockchain-testing-1.0-SNAPSHOT-jar-with-dependencies.jar 1 86400000
java -jar target/blockchain-testing-1.0-SNAPSHOT-jar-with-dependencies.jar 1 1000
cd ..

# delete generated cov files and restart rippled server to run tests
kill $(lsof -t -i:5005)
cd rippled-1.6.0
find . -type f -name "*.gcda" -delete 
cd build/cmake/coverage
./rippled -a -v --debug & disown
cd ../../../../
cd blockchain-testing
mvn clean test -Dtest=generated.ind*

# calculate test coverage and copy to host
k $(lsof -t -i:5005)
cd ..
cd rippled-1.6.0
gcovr -b -r ./ -o coverage_results.txt

echo "Fitness: <1> and time: <86400000>" > coverage_results.txt
docker cp rippled_tool_container:/rippled-1.6.0/coverage_results.txt .
