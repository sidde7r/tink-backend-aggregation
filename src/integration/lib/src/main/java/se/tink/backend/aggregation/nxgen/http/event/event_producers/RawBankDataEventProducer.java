package se.tink.backend.aggregation.nxgen.http.event.event_producers;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.http.event.configuration.RawBankDataEventEmissionConfiguration;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEvent;

public interface RawBankDataEventProducer {

    Optional<RawBankDataTrackerEvent> produceRawBankDataEvent(
            RawBankDataEventEmissionConfiguration rawBankDataEventEmissionConfiguration,
            String responseBody,
            String correlationId);
}
