package se.tink.backend.aggregation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.framework.wiremock.WireMockTestServer;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.AapFileParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.ResourceFileReader;

public class BlackboxWireMockServer {

    private static final Logger log = LoggerFactory.getLogger(BlackboxWireMockServer.class);
    private WireMockTestServer server;

    public static void main(String[] args) {
        new BlackboxWireMockServer().run();
    }

    public void run() {
        log.info("Starting WireMock server");
        server = new WireMockTestServer(10001, 10000);

        server.prepareMockServer(
                new AapFileParser(
                        new ResourceFileReader()
                                .read(
                                        "src/aggregation/service/src/test/java/se/tink/backend/aggregation/resources/amex.aap")));

        log.info("WireMock server is started");
    }
}
