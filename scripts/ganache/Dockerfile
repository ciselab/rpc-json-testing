FROM ubuntu:18.04

RUN rm /bin/sh && ln -s /bin/bash /bin/sh

RUN apt-get update && \
    apt-get -y install --no-install-recommends git default-jdk maven lsof procps curl sed && rm -rf /var/lib/apt/lists/*

# Install nvm based on https://github.com/nvm-sh/nvm
ENV NVM_DIR /usr/local/nvm
# ENV NVM_DIR ~/.nvm
ENV NODE_VERSION 14.17.3

RUN curl https://raw.githubusercontent.com/creationix/nvm/v0.30.1/install.sh | bash \
    && source $NVM_DIR/nvm.sh \
    && nvm install $NODE_VERSION \
    && nvm alias default $NODE_VERSION \
    && nvm use default

ENV NODE_PATH $NVM_DIR/v$NODE_VERSION/lib/node_modules
ENV PATH      $NVM_DIR/versions/node/v$NODE_VERSION/bin:$PATH

# Pull ganache-cli repo
RUN git clone https://github.com/trufflesuite/ganache-cli-archive.git ganache-cli

# Install nyc
RUN cd ganache-cli && npm install --save-dev nyc
RUN npm install -g nyc

# Add configuration for nyc (to compute coverage only for ganache-core)
COPY ./.nycrc ./ganache-cli/.nycrc
# Modify package.json
RUN cd ganache-cli && sed -i 's/"start": "node cli.js",/"start": "nyc --no-clean node cli.js --nycrc-path=.nycrc", \n    "start_clean": "nyc node cli.js --nycrc-path=.nycrc",/' package.json

# Pull tool repo and compile it
RUN git clone https://lisv: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@github.com/SERG-Delft/blockchain-testing.git && cd blockchain-testing && \
	git pull && \
	mvn clean install -DskipTests && \
	cp src/main/resources/ethereum-openrpc.json target/

# Give permission to run scripts
RUN cd blockchain-testing/scripts/ganache && chmod +x script.sh
RUN cd blockchain-testing/scripts/ganache && chmod +x startGanache.sh
RUN cd blockchain-testing/scripts/ganache && chmod +x coverageGanache.sh

ENTRYPOINT ["blockchain-testing/scripts/ganache/script.sh"]
CMD ["1", "100", "gens", "g", "0", "0.25"]