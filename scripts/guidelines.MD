# Instructions

## The Dockerfile can be build using the following command from inside the folder containing the Dockerfile: 
docker build -t <name_image>:V1.0.1 --no-cache .

## To then enter the docker image, use the following command:
#### The first argument is the fitness function used (1-8), 
#### The second argument is the time the experiment will run (in mins) 
#### The third argument is the server used (g for ganache or r for rippled).
docker run -it <name_image>:V1.0.1 $1 $2 $3
## Or to tag it with a name:
docker run -it --name $3_container_$1_$2 <name_image>:V1.0.1 $1 $2 $3
