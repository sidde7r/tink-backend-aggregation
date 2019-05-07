FROM gcc:9.1

COPY . /usr/src/libkbc_wbaes

WORKDIR /usr/src/libkbc_wbaes

RUN apt-get update && apt-get install -y libm4ri-dev && rm -rf /var/lib/apt/lists/*

CMD ["/bin/bash","-c","make && make clean"]
