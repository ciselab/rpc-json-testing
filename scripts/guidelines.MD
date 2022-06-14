# Instructions to build docker image and run containers
<ol>
<li>The Dockerfile can be build using the following command from inside the folder containing the Dockerfile:
 
`docker build -t <name_image> --no-cache .`

* For *rippled* use the rippled Dockerfile (in the rippled folder).
* For *ganache* use the ganache Dockerfile (in the ganache folder). 
**Important: for ganache the *.nycrc* file must be inside the same folder as the Dockerfile as this file is used to build the image.**
</li>
-----------
<li>
To create a docker container based on the image manually, use the following command. 

**Note that this requires the docker image to have an entry point. If not, go to Step 3.**

`docker run -it <name_image> $1 $2 $3 $4 $5 $6 $7 $8`

Or to tag the container with a name (makes it easier to access later):

`docker run -it --name <name_container> <name_image> $1 $2 $3 $4 $5 $6 $7 $8`
* The first argument $1 is the fitness function used (1-8).
* $2 is the budget of the experiment (mins/evals/generations depending on budget type) (in mins, e.g. 30)
* $3 is the budget type used (time, evals, or gens).
* $4 is the server used (g for ganache or r for rippled).
* $5 is the population size.
* $6 is the proportion of the population that will be mutated (rather than a new individual being generated to replace the individual) (between 0 and 1, e.g. 0.5).
* $7 is the probability that a param will change type when mutated.
* $8 is the amount of generations to be processed before clustering again (only for diversity based fitness).

</li>
-----------
<li>

The commands at Step 3 assume that the Docker image was built with an entry point. If this is not the case, use the following commands. Otherwise, continue with Step 4.

`docker run -it <name_image>`

And then for an experiment on the rippled server:

`./blockchain-testing/scripts/rippled/script.sh $1 $2 $3 $4 $5 $6 $7 $8`

Or for an experiment on the ganache server:

`./blockchain-testing/scripts/ganache/script.sh $1 $2 $3 $4 $5 $6 $7 $8`

An example would be: `./blockchain-testing/scripts/ganache/script.sh 8 30 t g 100 0.5 0.25 3`. 
This would lead to an experiment on the ganache server with the evolutionary fuzzer for 30 minutes, with a population size of 100, 50% mutation proportion, 25% probability of type change and clustering after 3 generations).

</li>
-----------
<li>
After running the commands specified at Step 2 or 3, the experiment will start (specifically, script.sh is called and executed). 

After the experiment has (successfully) finished, the following files are generated:

* /blockchain-testing/coverage-over-time.txt (shows branch and line coverage (%) of every 5 mins)
* /blockchain-testing/output/archive_size.txt (includes final archive size and number of generations processed until stop)
* /blockchain-testing/output/methods_archive.txt
* /blockchain-testing/output/methods_per_gen.txt
* /blockchain-testing/output/methods_total.txt
* /blockchain-testing/output/status_codes_archive.txt
* /blockchain-testing/output/status_codes_per gen.txt
* /blockchain-testing/output/status_codes_total.txt

And specifically for rippled:
* /rippled-1.6.0/final_bcoverage_total.txt (branch coverage report after experiment)
* /rippled-1.6.0/final_lcoverage_total.txt (line coverage report after experiment)
* /rippled-1.6.0/final_bcoverage_archive.txt (branch coverage report based on tests in archive)
* /rippled-1.6.0/final_lcoverage_archive.txt (line coverage report based on tests in archive)

Or for ganache:
* /ganache-cli/final_coverage_total.txt (branch and line coverage report after experiment)
* /ganache-cli/final_coverage_archive.txt (branch and line coverage report based on tests in archive)


</li>
-----------

<li>
The files can be viewed from inside the docker container and/or they can be copied to the host machine. 

* To access the docker container, use the following commands:

`docker start <container_name>` (to start the container again if the experiment finished already and the container was stopped)

`docker exec -it <container_name> /bin/bash` (to get inside the container)

* Or to copy the files to the host machine directly, use the following command from outside the docker container: 

`mkdir <folder_name>`

`docker cp <container_name>:<path_to_file_in_container> ./<folder_name>/`

The `folder_name` can be an identifiable name so it is clear to what experiment the results correspond to.

The `path_to_file_in_container` is for example `/ganache-cli/final_coverage_total.txt` or `/blockchain-testing/coverage-over-time.txt`). The specific paths for the files are specified in Step 3.

</li>

</ol>
