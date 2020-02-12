package se.tink.backend.aggregation.events;

import javax.inject.Inject;
import se.tink.libraries.event_producer_service_client.grpc.EventProducerServiceClient;

public class DataTrackerEventProducer {

    private final EventProducerServiceClient eventProducerServiceClient;

    @Inject
    public DataTrackerEventProducer(EventProducerServiceClient eventProducerServiceClient) {
        this.eventProducerServiceClient = eventProducerServiceClient;
    }
}
