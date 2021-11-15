package se.tink.backend.aggregation.nxgen.raw_data_events.interceptor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.raw_data_events.decision_strategy.RawBankDataEventCreationTriggerStrategy;
import se.tink.backend.aggregation.nxgen.raw_data_events.event_producers.RawBankDataEventAccumulator;
import se.tink.backend.aggregation.nxgen.raw_data_events.event_producers.RawBankDataEventProducer;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEvent;
import se.tink.libraries.credentials.service.RefreshableItem;

public class RawBankDataEventProducerInterceptor extends Filter {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(RawBankDataEventProducerInterceptor.class);

    private final RawBankDataEventProducer rawBankDataEventProducer;
    private final RawBankDataEventAccumulator rawBankDataEventAccumulator;
    private final String correlationId;
    private final String providerName;

    private RawBankDataEventCreationTriggerStrategy rawBankDataEventCreationTriggerStrategy;
    private final Supplier<RefreshableItem> refreshableItemInProgressSupplier;

    public RawBankDataEventProducerInterceptor(
            RawBankDataEventProducer rawBankDataEventProducer,
            RawBankDataEventAccumulator rawBankDataEventAccumulator,
            Supplier<RefreshableItem> refreshableItemInProgressSupplier,
            String correlationId,
            String providerName,
            RawBankDataEventCreationTriggerStrategy rawBankDataEventCreationTriggerStrategy) {
        this.rawBankDataEventProducer = rawBankDataEventProducer;
        this.rawBankDataEventAccumulator = rawBankDataEventAccumulator;
        this.correlationId = correlationId;
        this.providerName = providerName;
        this.refreshableItemInProgressSupplier = refreshableItemInProgressSupplier;
        this.rawBankDataEventCreationTriggerStrategy = rawBankDataEventCreationTriggerStrategy;
    }

    public void overrideRawBankDataEventCreationTriggerStrategy(
            RawBankDataEventCreationTriggerStrategy rawBankDataEventCreationTriggerStrategy) {
        this.rawBankDataEventCreationTriggerStrategy = rawBankDataEventCreationTriggerStrategy;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        /*
           If refreshableItemInProgressSupplier returns null, it means that we are in
           authentication stage and for this stage we don't want to emit raw bank data events
        */
        if (Objects.isNull(refreshableItemInProgressSupplier.get())) {
            return response;
        }

        // Log the content-types for all HTTP responses (this is to detect what response types
        // we are facing)
        try {
            List<String> contentType = response.getHeaders().get("Content-Type");
            LOGGER.info("[RESPONSE CONTENT TYPE] Detected content-type: {}", contentType);
        } catch (Exception e) {
            LOGGER.warn(
                    "[RESPONSE CONTENT TYPE] Could not detect the content-type for HTTP response",
                    e);
        }

        // Check if the decision strategy tells us to produce an event or not
        if (rawBankDataEventCreationTriggerStrategy.shouldTryProduceRawBankDataEvent()) {
            try {
                String rawResponseString = response.getBody(String.class);
                /*
                We need to call reset method again because if an agent first calls getBody() method
                and consume the input stream and then calls getBodyInputStream() method to read
                a binary response, since getBody would have consumed the stream, getBodyInputStream()
                would have an empty stream. As the raw bank data event emission filter will always
                try to read the response by calling getBody(), in order to ensure that the stream
                will still be consumable, we reset the stream to allow multiple reads. When we will
                be sure that all works fine we can move this code to JerseyHttpResponse class
                (in getBody method)
                 */
                response.getInternalResponse().getEntityInputStream().reset();
                Optional<RawBankDataTrackerEvent> maybeEvent =
                        rawBankDataEventProducer.produceRawBankDataEvent(
                                rawResponseString, correlationId, providerName);
                maybeEvent.ifPresent(
                        event ->
                                rawBankDataEventAccumulator.addEvent(
                                        event, refreshableItemInProgressSupplier.get()));
            } catch (Exception e) {
                LOGGER.warn(
                        "[RawBankDataEventProducerInterceptor] Could not intercept HTTP response for raw bank data event production");
            }
        }

        return response;
    }
}
