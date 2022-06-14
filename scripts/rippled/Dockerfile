FROM ubuntu:18.04

# Install required packages and boost.
RUN apt-get -y update && \
	apt-get -y install --no-install-recommends git pkg-config protobuf-compiler libprotobuf-dev libssl-dev wget build-essential unzip python3 gcovr default-jdk maven lsof procps && rm -rf /var/lib/apt/lists/* && \
	wget https://lisv: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@github.com/Kitware/CMake/releases/download/v3.13.3/cmake-3.13.3-Linux-x86_64.sh && \
	sh cmake-3.13.3-Linux-x86_64.sh --prefix=/usr/local --exclude-subdir && \
	wget https://boostorg.jfrog.io/artifactory/main/release/1.71.0/source/boost_1_71_0.tar.gz --no-check-certificate && \
    tar xvzf boost_1_71_0.tar.gz && \
	cd boost_1_71_0 && ./bootstrap.sh && cd .. && \
	cd boost_1_71_0 && ./b2 -j 4 || true && cd ..
ENV BOOST_ROOT /boost_1_71_0

# Pull rippled repo and configure server.
RUN wget https://lisv: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@github.com/ripple/rippled/archive/refs/tags/1.6.0.zip && \
	mv 1.6.0.zip rippled-1.6.0.zip && \
	unzip rippled-1.6.0.zip
RUN cd rippled-1.6.0 && \
	python3 ./Builds/Test.py -v --dir coverage -- -j 8

RUN mkdir -p /root/.config/ripple && \
	cp ./rippled-1.6.0/cfg/rippled-example.cfg /root/.config/ripple/rippled.cfg && \
	cp ./rippled-1.6.0/cfg/validators-example.txt /root/.config/ripple/validators.txt && \
	find ./rippled-1.6.0 -type f -name "*.gcda" -delete
	
# Pull tool repo and compile it.
RUN git clone https://lisv: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@github.com/SERG-Delft/blockchain-testing.git && cd blockchain-testing && \
	git pull && \
	mvn clean install -DskipTests && \
	cp src/main/resources/ripple-openrpc.json target/ && \
	cp src/main/resources/ripple-openrpc-small.json target/
	
# Give permission to run scripts.
RUN cd blockchain-testing/scripts/rippled && chmod +x script.sh
RUN cd blockchain-testing/scripts/rippled && chmod +x startRippled.sh
RUN cd blockchain-testing/scripts/rippled && chmod +x coverageRippled.sh

ENTRYPOINT ["blockchain-testing/scripts/rippled/script.sh"]
CMD ["1", "100", "gens", "r", "50", "0", "0.25", "3"]