#!/bin/bash

# Before running this script, the docker image must be built. This can be done using 'docker build -t rippled --no-cache .' in the directory where the Dockerfile is stored.

# Run the container, which automatically starts the script with arguments $1 (fitnessfunction), $2 (timelimit), $3 (server used) and $4 (proportion of mutation instead of generation).
docker run -it --name $3_container_$1_$2_$4 rippled $1 $2 $3 $4

# Copy results to host (folder must exist before putting something in there). 
# Gives it a unique name so each experiment has a different identifiable folder (e.g. exp_r_2_60_0.1 where r = rippled server, fitness function = 2, time = 60 minutes, and proportion of mutation = 10%).
mkdir exp_$3_$1_$2_$4
docker cp $3_container_$1_$2_$4:/rippled-1.6.0/final_bcoverage_total.txt ./exp_$3_$1_$2_$4/
docker cp $3_container_$1_$2_$4:/rippled-1.6.0/final_lcoverage_total.txt ./exp_$3_$1_$2_$4/
docker cp $3_container_$1_$2_$4:/rippled-1.6.0/final_bcoverage_archive.txt ./exp_$3_$1_$2_$4/
docker cp $3_container_$1_$2_$4:/rippled-1.6.0/final_lcoverage_archive.txt ./exp_$3_$1_$2_$4/

docker cp $3_container_$1_$2_$4:/blockchain-testing/coverage_over_time.txt ./exp_$3_$1_$2_$4/
docker cp $3_container_$1_$2_$4:/blockchain-testing/fitness_progress.txt ./exp_$3_$1_$2_$4/
docker cp $3_container_$1_$2_$4:/blockchain-testing/archive_size.txt ./exp_$3_$1_$2_$4/
docker cp $3_container_$1_$2_$4:/blockchain-testing/best_fitness_values.txt ./exp_$3_$1_$2_$4/
docker cp $3_container_$1_$2_$4:/blockchain-testing/methods_per_gen.txt ./exp_$3_$1_$2_$4/
docker cp $3_container_$1_$2_$4:/blockchain-testing/methods__total.txt ./exp_$3_$1_$2_$4/
docker cp $3_container_$1_$2_$4:/blockchain-testing/methods__archive.txt ./exp_$3_$1_$2_$4/
docker cp $3_container_$1_$2_$4:/blockchain-testing/status_codes_per_gen.txt ./exp_$3_$1_$2_$4/
docker cp $3_container_$1_$2_$4:/blockchain-testing/status_codes_total.txt ./exp_$3_$1_$2_$4/
docker cp $3_container_$1_$2_$4:/blockchain-testing/status_codes_archive.txt ./exp_$3_$1_$2_$4/

docker cp $3_container_$1_$2_$4:/blockchain-testing/src/test/java/generated/. ./exp_$3_$1_$2_$4/tests/
