#!/bin/bash

# Before running this script, the docker image must be built. This can be done using 'docker build -t rippled_tool:V1 --no-cache .' in the directory where the Dockerfile is stored.

# Run the container, which automatically starts the script with arguments $1 (fitnessfunction) and $2 (timelimit).
docker run -it --name rippled_tool_container_$3_$1_$2 rippled_tool:V1.0.1 $1 $2 $3

# Copy results to host (folder must exist before putting something in there). 
# Gives it a unique name so each experiment has a different identiable folder (e.g. exp_2_60 where fitness function = 2 and time = 60 minutes).
mkdir exp_$3_$1_$2
docker cp rippled_tool_container_$3_$1_$2:/rippled-1.6.0/final_bcoverage_total.txt ./exp_$3_$1_$2/
docker cp rippled_tool_container_$3_$1_$2:/rippled-1.6.0/final_lcoverage_total.txt ./exp_$3_$1_$2/
docker cp rippled_tool_container_$3_$1_$2:/rippled-1.6.0/final_bcoverage_archive.txt ./exp_$3_$1_$2/
docker cp rippled_tool_container_$3_$1_$2:/rippled-1.6.0/final_lcoverage_archive.txt ./exp_$3_$1_$2/

docker cp rippled_tool_container_$3_$1_$2:/blockchain-testing/coverage_over_time.txt ./exp_$3_$1_$2/
docker cp rippled_tool_container_$3_$1_$2:/blockchain-testing/fitness_progress.txt ./exp_$3_$1_$2/
docker cp rippled_tool_container_$3_$1_$2:/blockchain-testing/status_codes_total.txt ./exp_$3_$1_$2/
docker cp rippled_tool_container_$3_$1_$2:/blockchain-testing/status_codes_archive.txt ./exp_$3_$1_$2/
docker cp rippled_tool_container_$3_$1_$2:/blockchain-testing/archive_size.txt ./exp_$3_$1_$2/
docker cp rippled_tool_container_$3_$1_$2:/blockchain-testing/best_fitness_values.txt ./exp_$3_$1_$2/

docker cp rippled_tool_container_$3_$1_$2:/blockchain-testing/src/test/java/generated/. ./exp_$3_$1_$2/tests/
