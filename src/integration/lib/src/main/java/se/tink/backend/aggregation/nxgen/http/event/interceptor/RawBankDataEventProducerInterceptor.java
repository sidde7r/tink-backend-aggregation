package se.tink.backend.aggregation.nxgen.http.event.interceptor;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.http.event.configuration.RawBankDataEventEmissionConfiguration;
import se.tink.backend.aggregation.nxgen.http.event.event_producers.RawBankDataEventAccumulator;
import se.tink.backend.aggregation.nxgen.http.event.event_producers.RawBankDataEventProducer;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEvent;

public class RawBankDataEventProducerInterceptor extends Filter {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(RawBankDataEventProducerInterceptor.class);

    private final RawBankDataEventProducer rawBankDataEventProducer;
    private final RawBankDataEventAccumulator rawBankDataEventAccumulator;
    private final String correlationId;

    private RawBankDataEventEmissionConfiguration rawBankDataEventEmissionConfiguration;

    public RawBankDataEventProducerInterceptor(
            RawBankDataEventProducer rawBankDataEventProducer,
            RawBankDataEventAccumulator rawBankDataEventAccumulator,
            String correlationId,
            RawBankDataEventEmissionConfiguration rawBankDataEventEmissionConfiguration) {
        this.rawBankDataEventProducer = rawBankDataEventProducer;
        this.rawBankDataEventAccumulator = rawBankDataEventAccumulator;
        this.correlationId = correlationId;
        this.rawBankDataEventEmissionConfiguration = rawBankDataEventEmissionConfiguration;
    }

    public void overrideRawBankDataEventEmissionConfiguration(
            RawBankDataEventEmissionConfiguration rawBankDataEventEmissionConfiguration) {
        this.rawBankDataEventEmissionConfiguration = rawBankDataEventEmissionConfiguration;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        // Check if the decision strategy tells us to produce an event or not
        if (rawBankDataEventEmissionConfiguration
                .getEmissionDecisionStrategy()
                .shouldEmitRawBankDataEvent()) {
            try {
                String rawResponseString = response.getBody(String.class);
                Optional<RawBankDataTrackerEvent> maybeEvent =
                        rawBankDataEventProducer.produceRawBankDataEvent(
                                rawBankDataEventEmissionConfiguration,
                                rawResponseString,
                                correlationId);
                maybeEvent.ifPresent(rawBankDataEventAccumulator::addEvent);
            } catch (Exception e) {
                LOGGER.warn(
                        "[RawBankDataEventProducerInterceptor] Could not intercept HTTP response for raw bank data event emission");
            }
        }

        return response;
    }
}
