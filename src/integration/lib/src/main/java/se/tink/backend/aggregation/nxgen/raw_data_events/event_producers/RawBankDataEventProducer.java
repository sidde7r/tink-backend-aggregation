package se.tink.backend.aggregation.nxgen.raw_data_events.event_producers;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.raw_data_events.configuration.RawBankDataEventCreationStrategies;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEvent;

public interface RawBankDataEventProducer {

    Optional<RawBankDataTrackerEvent> produceRawBankDataEvent(
            String responseBody, String correlationId, String providerName);

    void overrideRawBankDataEventCreationStrategies(
            RawBankDataEventCreationStrategies rawBankDataEventCreationStrategies);
}
