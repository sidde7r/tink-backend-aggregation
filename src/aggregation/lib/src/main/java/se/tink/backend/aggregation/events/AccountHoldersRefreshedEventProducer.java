package se.tink.backend.aggregation.events;

import com.google.protobuf.Any;
import java.time.Instant;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.eventproducerservice.events.grpc.AccountHoldersRefreshedEventProto;
import se.tink.libraries.event_producer_service_client.grpc.EventProducerServiceClient;
import se.tink.libraries.serialization.proto.utils.ProtobufTypeUtil;

public class AccountHoldersRefreshedEventProducer {
    private final EventProducerServiceClient eventProducerServiceClient;
    private final boolean eventEnabled;
    private static final Logger log =
            LoggerFactory.getLogger(AccountHoldersRefreshedEventProducer.class);

    @Inject
    public AccountHoldersRefreshedEventProducer(
            @Named("sendAccountHoldersRefreshedEvents") boolean eventEnabled,
            EventProducerServiceClient eventProducerServiceClient) {
        this.eventEnabled = eventEnabled;
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
        if (!eventEnabled) {
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
            log.warn("could not produce event: AccountHoldersRefreshedEvent", e);
        }
    }
}
