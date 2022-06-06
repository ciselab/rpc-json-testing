#!/bin/bash

# Reset coverage
cd /rippled-1.6.0
find . -type f -name "*.gcda" -delete

# Run the tool
cd /blockchain-testing
# The first argument is the fitness function used (1-8),
# The second arg is the budget of the experiment (mins/evals/generations depending on budget type)
# The third arg is the budget type used (time, evals, or gens)
# The fourth arg is the server used (g or r).
# The fifth argument is the proportion of individuals to be mutated.
# The sixth argument is the probability that a param will change type when mutated.
java -jar target/blockchain-testing-1.0-SNAPSHOT-jar-with-dependencies.jar $1 $2 $3 $4 $5 $6

# Kill current rippled server at port 5005
id=$(lsof -t -i:5005 -sTCP:LISTEN)
echo "killing: $id"
kill "$id"
while [ "$(lsof -t -i:5005 -sTCP:LISTEN)" != "" ]; do
 echo "waiting at kill...  -$(lsof -t -i:5005 -sTCP:LISTEN)-"
 sleep 0.1
done

# Compute test coverage achieved by running the tool
cd /rippled-1.6.0
gcovr -s -b -r ./ -f 'src/ripple' -o final_bcoverage_total.txt
echo "Server: " $4", heuristic:" $1 ", mutation proportion: " $5 ", type change probability: " $6 " and budget:" $2 " " $3 >> final_bcoverage_total.txt cat final_bcoverage_total.txt
gcovr -s -r ./ -f 'src/ripple' -o final_lcoverage_total.txt
echo "Server: " $4", heuristic:" $1 ", mutation proportion: " $5 ", type change probability: " $6 " and budget:" $2 " " $3 >> final_lcoverage_total.txt cat final_lcoverage_total.txt

# Reset coverage and run the generated tests
find . -type f -name "*.gcda" -delete 
cd /blockchain-testing
mvn clean test -Dtest=generated.ind*

# Kill current rippled server at port 5005
id=$(lsof -t -i:5005 -sTCP:LISTEN)
echo "killing: $id"
kill "$id"
while [ "$(lsof -t -i:5005 -sTCP:LISTEN)" != "" ]; do
 echo "waiting at kill...  -$(lsof -t -i:5005 -sTCP:LISTEN)-"
 sleep 0.1
done

# Compute test coverage achieved by running the generated tests
cd /rippled-1.6.0
gcovr -s -b -r ./ -f 'src/ripple' -o final_bcoverage_archive.txt
echo "Server: " $4", heuristic:" $1 ", mutation proportion: " $5 ", type change probability: " $6 " and budget:" $2 " " $3 >> final_bcoverage_archive.txt cat final_bcoverage_archive.txt
gcovr -s -r ./ -f 'src/ripple' -o final_lcoverage_archive.txt
echo "Server: " $4", heuristic:" $1 ", mutation proportion: " $5 ", type change probability: " $6 " and budget:" $2 " " $3 >> final_lcoverage_archive.txt cat final_lcoverage_archive.txt

exit
