package se.tink.backend.aggregation.nxgen.http.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FakeNextGenTinkHttpClientEventProducer implements NextGenTinkHttpClientEventProducer {

    private static final Logger log =
            LoggerFactory.getLogger(FakeNextGenTinkHttpClientEventProducer.class);

    @Override
    public void dummyMethod() {
        log.info("FakeNextGenTinkHttpClientEventProducer called");
    }
}
