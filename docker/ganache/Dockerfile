# Use base image that contains node and nvm
FROM node:erbium

# Install required packages
RUN apt-get -y update && \
	apt-get -y upgrade && \
	apt-get -y install git default-jdk maven curl nano sed && rm -rf /var/lib/apt/lists/*

# Clone ganache-cli and install nyc there
RUN git clone https://github.com/trufflesuite/ganache-cli.git && \
	cd ganache-cli && \
	npm install --save-dev nyc
# Add configuration for nyc (to compute coverage only for ganache-core) 
COPY ./.nycrc ./ganache-cli/.nycrc

# Modify package.json.
RUN cd ganache-cli && sed -i 's/"start": "node cli.js",/"start": "nyc --no-clean node cli.js --nycrc-path=.nycrc",/' package.json 

# Pull tool repo and compile it.
RUN git clone https://lisv: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@github.com/SERG-Delft/blockchain-testing.git && cd blockchain-testing && \
	git pull && \
	mvn clean install -DskipTests && \
	cp src/main/resources/ethereum-openrpc.json target/
	
# Give permission to run scripts.
RUN cd blockchain-testing/docker/ganache && chmod +x script.sh
RUN cd blockchain-testing/scripts && chmod +x startGanache.sh