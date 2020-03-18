package se.tink.backend.fakebank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.framework.wiremock.WireMockTestServer;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.AapFileParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.ResourceFileReader;

public class FakeBankServer {

    private static final Logger log = LoggerFactory.getLogger(FakeBankServer.class);
    private WireMockTestServer server;

    public static void main(String[] args) {
        new FakeBankServer().run();
    }

    public void run() {
        log.info("Starting WireMock server");
        server = new WireMockTestServer(10001, 10000);

        server.prepareMockServer(
                new AapFileParser(
                        new ResourceFileReader()
                                .read(
                                        "src/aggregation/fakebank/src/main/java/se/tink/backend/fakebank/resources/amex.aap")));

        log.info("WireMock server is started");
    }
}
