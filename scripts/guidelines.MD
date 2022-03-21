# Instructions to build docker image and run containers
<ol>
<li>The Dockerfile can be build using the following command from inside the folder containing the Dockerfile:
 
`docker build -t <name_image> --no-cache .`

* For *rippled* use the rippled Dockerfile (in the rippled folder).
* For *ganache* use the ganache Dockerfile (in the ganache folder). 
**Important: for ganache the *.nycrc* file must be inside the same folder as the Dockerfile as this file is used to build the image.**

The easiest way to start an experiment is to run the `runexperiment.sh $1 $2 $3 $4` script. This requires the `<name_image>` to be `rippled` or `ganache` depending on the server to be tested (as this is hardcoded in the script). The arguments are the same as specified in Step 2. After running this script, the results will be automatically copied onto the host computer.

If you want more insights into what is going on, it is best to run the experiment manually. How to do this is explained below.
</li>
-----------
<li>
To then create a docker container based on the image, use the following command:

`docker run -it <name_image> $1 $2 $3 $4`

Or to tag the container with a name (makes it easier to access later):

`docker run -it --name <name_container> <name_image> $1 $2 $3 $4`
* The first argument $1 is the fitness function used (the only option is 1: the grammar-based fuzzer), 
* The second argument $2 is the time the experiment will run (in mins).
* The third argument $3 is the server used (g for ganache or r for rippled).
* The fourth argument $4 is the proportion of the population that will be mutated (rather than a new individual being generated to replace the individual).

</li>
-----------
<li>
After running the command specified at Step 2, the experiment will start (specifically, script.sh is called and executed). 

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

* To access the running docker container, use the following commands:

`docker start <container_name>` (to start the container again if the experiment finished already)

`docker exec -it <container_name> /bin/bash`

* Or to copy the files to the host machine directly, use the following command from outside the docker container: 

`mkdir <folder_name>`

`docker cp <container_name>:<path_to_file_in_container> ./<folder_name>/`

The `folder_name` can be an identifiable name so it is clear to what experiment the results correspond to.

The `path_to_file_in_container` is for example `/ganache-cli/final_coverage_total.txt` or `/blockchain-testing/coverage-over-time.txt`). The specific paths for the files are specified in Step 3.

All the commands (to copy each file) are also specified in the `runexperiment.sh` file.
</li>

</ol>
