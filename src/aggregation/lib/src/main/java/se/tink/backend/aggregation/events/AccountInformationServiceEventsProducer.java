package se.tink.backend.aggregation.events;

import com.google.protobuf.Any;
import java.time.Instant;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.eventproducerservice.events.grpc.AccountHoldersRefreshedEventProto;
import se.tink.libraries.event_producer_service_client.grpc.EventProducerServiceClient;
import se.tink.libraries.serialization.proto.utils.ProtobufTypeUtil;

public class AccountInformationServiceEventsProducer {
    private final EventProducerServiceClient eventProducerServiceClient;
    private final boolean eventsEnabled;
    private static final Logger log =
            LoggerFactory.getLogger(AccountInformationServiceEventsProducer.class);

    @Inject
    public AccountInformationServiceEventsProducer(
            @Named("accountInformationServiceEvents") boolean eventsEnabled,
            EventProducerServiceClient eventProducerServiceClient) {
        this.eventsEnabled = eventsEnabled;
        this.eventProducerServiceClient = eventProducerServiceClient;
    }

    public void sendAccountHoldersRefreshedEvent(
            String clusterId,
            String appId,
            String userId,
            String providerName,
            String correlationId,
            String accountId,
            String holderType,
            int holdersCount) {
        if (!eventsEnabled) {
            return;
        }
        try {
            AccountHoldersRefreshedEventProto.AccountHoldersRefreshedEvent.Builder builder =
                    AccountHoldersRefreshedEventProto.AccountHoldersRefreshedEvent.newBuilder()
                            .setTimestamp(ProtobufTypeUtil.toProtobufTimestamp(Instant.now()))
                            .setClusterId(clusterId)
                            .setAppId(appId)
                            .setUserId(userId)
                            .setProviderName(providerName)
                            .setCorrelationId(correlationId)
                            .setAccountId(accountId)
                            .setHoldersCount(holdersCount);

            Optional.ofNullable(holderType).ifPresent(builder::setHolderType);

            AccountHoldersRefreshedEventProto.AccountHoldersRefreshedEvent event = builder.build();

            eventProducerServiceClient.postEventFireAndForget(Any.pack(event));
        } catch (RuntimeException e) {
            log.warn("Could not produce event: AccountHoldersRefreshedEvent", e);
        }
    }

    public void sendPsd2PaymentAccountClassificationEvent(
            String clusterId,
            String appId,
            String userId,
            String providerName,
            String marketCode,
            String correlationId,
            String credentialsId,
            String accountId,
            String accountType,
            String classificationResult,
            AccountCapabilities accountCapabilities) {
        // will be finished once https://github.com/tink-ab/tink-grpc/pull/698 is merged
    }
}
