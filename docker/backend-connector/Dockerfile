FROM tink/base-java:8

RUN wget -O /usr/bin/dumb-init https://github.com/Yelp/dumb-init/releases/download/v1.2.0/dumb-init_1.2.0_amd64
RUN chmod +x /usr/bin/dumb-init

RUN mkdir /heapdumps
RUN mkdir /gc-logs
COPY docker/service-wrapper.sh /

ENV SERVICE_NAME=connector
ENV SERVICE_DIR=/usr/share/tink-backend-connector

COPY tools ${SERVICE_DIR}/tools
COPY data ${SERVICE_DIR}/data

COPY src/${SERVICE_NAME}-service/target/lib ${SERVICE_DIR}/lib
COPY src/${SERVICE_NAME}-service/target/${SERVICE_NAME}-service.jar ${SERVICE_DIR}/

ENV MAX_HEAP_SIZE=2560m

ENTRYPOINT ["/usr/bin/dumb-init", "--", "/service-wrapper.sh"]
CMD ["server", "/etc/tink/connector-server.yml"]
