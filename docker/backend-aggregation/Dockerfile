FROM tink/base-java:8

RUN wget -O /usr/bin/dumb-init https://github.com/Yelp/dumb-init/releases/download/v1.2.0/dumb-init_1.2.0_amd64
RUN chmod +x /usr/bin/dumb-init

RUN apt-get update && apt-get install -y xvfb libtesseract4 libm4ri-dev && rm -rf /var/lib/apt/lists/*

RUN mkdir /heapdumps
RUN mkdir /gc-logs
COPY docker/service-wrapper.sh /

ENV SERVICE_NAME=aggregation
ENV SERVICE_DIR=/usr/share/tink-backend-aggregation

COPY tools ${SERVICE_DIR}/tools
COPY data ${SERVICE_DIR}/data

COPY src/${SERVICE_NAME}-service/target/lib ${SERVICE_DIR}/lib
COPY src/${SERVICE_NAME}-service/target/${SERVICE_NAME}_service.jar ${SERVICE_DIR}/

ENV MAX_HEAP_SIZE=2560m

ENTRYPOINT ["/usr/bin/dumb-init", "--", "/service-wrapper.sh", "--start-x"]
CMD ["server", "/etc/tink/aggregation-server.yml"]
