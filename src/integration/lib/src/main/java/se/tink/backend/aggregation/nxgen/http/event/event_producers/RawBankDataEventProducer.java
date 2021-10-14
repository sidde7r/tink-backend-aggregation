package se.tink.backend.aggregation.nxgen.http.event.event_producers;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.http.event.configuration.RawBankDataEventCreationStrategies;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEvent;

public interface RawBankDataEventProducer {

    Optional<RawBankDataTrackerEvent> produceRawBankDataEvent(
            String responseBody, String correlationId);

    void overrideRawBankDataEventCreationStrategies(
            RawBankDataEventCreationStrategies rawBankDataEventCreationStrategies);
}
