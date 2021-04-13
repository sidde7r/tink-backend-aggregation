package se.tink.backend.aggregation.nxgen.http.event;

import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.events.guice.EventSubmitterProvider;

public class DefaultNextGenTinkHttpClientEventProducer
        implements NextGenTinkHttpClientEventProducer {

    private static final Logger log =
            LoggerFactory.getLogger(DefaultNextGenTinkHttpClientEventProducer.class);

    @Inject
    public DefaultNextGenTinkHttpClientEventProducer(
            EventSubmitterProvider eventSubmitterProvider) {}

    @Override
    public void dummyMethod() {
        log.info("DefaultNextGenTinkHttpClientEventProducer called");
    }
}
