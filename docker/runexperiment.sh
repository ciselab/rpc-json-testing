#!/bin/bash

# Before running this script, the docker image must be built. This can be done using 'docker build -t rippled_tool:V1 --no-cache .' in the directory where the Dockerfile is stored.

# Run the container, which automatically starts the script with arguments $1 (fitnessfunction) and $2 (timelimit).
docker run -it --name rippled_tool_container_$1_$2 rippled_tool:V1.0.0 $1 $2

# Copy results to host (folder must exist before putting something in there). 
# Gives it a unique name so each experiment has a different identiable folder (e.g. exp_2_60 where fitness function = 2 and time = 60 minutes).
mkdir exp_$1_$2
docker cp rippled_tool_container_$1_$2:/rippled-1.6.0/coverage_results_no_archive.txt ./exp_$1_$2/
docker cp rippled_tool_container_$1_$2:/rippled-1.6.0/coverage_results.txt ./exp_$1_$2/
docker cp rippled_tool_container_$1_$2:/blockchain-testing/src/test/java/generated/. ./exp_$1_$2/tests/
docker cp rippled_tool_container_$1_$2:/blockchain-testing/archiveSize_bestFitnessValues.txt ./exp_$1_$2/