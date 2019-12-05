package se.tink.backend.aggregation.events;

import com.google.common.base.Strings;
import com.google.protobuf.Any;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.eventproducerservice.events.grpc.CredentialsRefreshCommandChainFinishedProto.CredentialsRefreshCommandChainFinished;
import se.tink.eventproducerservice.events.grpc.CredentialsRefreshCommandChainStartedProto.CredentialsRefreshCommandChainStarted;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.event_producer_service_client.grpc.EventProducerServiceClient;
import se.tink.libraries.serialization.proto.utils.ProtobufTypeUtil;

public class CredentialsEventProducer {

    private final EventProducerServiceClient eventProducerServiceClient;

    @Inject
    public CredentialsEventProducer(EventProducerServiceClient eventProducerServiceClient) {
        this.eventProducerServiceClient = eventProducerServiceClient;
    }

    public void sendCredentialsRefreshCommandChainFinished(
            Credentials credentials,
            String appId,
            String correlationId,
            String clusterId,
            boolean manual) {
        CredentialsRefreshCommandChainFinished data =
                CredentialsRefreshCommandChainFinished.newBuilder()
                        .setTimestamp(ProtobufTypeUtil.toProtobufTimestamp(Instant.now()))
                        .setAppId(Strings.nullToEmpty(appId))
                        .setUserId(credentials.getUserId())
                        .setCredentialsId(credentials.getId())
                        .setCredentialStatus(
                                CredentialsStatusConverter.convert(credentials.getStatus()))
                        .setProviderName(credentials.getProviderName())
                        .setCorrelationId(correlationId)
                        .setClusterId(clusterId)
                        .setManual(manual)
                        .setCredentialsType(
                                CredentialsTypeFinishedEventConverter.convert(
                                        credentials.getType()))
                        .build();

        eventProducerServiceClient.postEventAsync(Any.pack(data));
    }

    public void sendCredentialsRefreshCommandChainStarted(
            Credentials credentials,
            String appId,
            String correlationId,
            String clusterId,
            boolean manual,
            Set<RefreshableItem> refreshableItems) {
        CredentialsRefreshCommandChainStarted data =
                CredentialsRefreshCommandChainStarted.newBuilder()
                        .setTimestamp(ProtobufTypeUtil.toProtobufTimestamp(Instant.now()))
                        .setAppId(Strings.nullToEmpty(appId))
                        .setUserId(credentials.getUserId())
                        .setCredentialsId(credentials.getId())
                        .setProviderName(credentials.getProviderName())
                        .setCorrelationId(correlationId)
                        .setClusterId(clusterId)
                        .setManual(manual)
                        .setCredentialsType(
                                CredentialsTypeStartedEventConverter.convert(credentials.getType()))
                        .addAllRefreshableItems(
                                refreshableItems.stream()
                                        .map(RefreshableItemsConverter::convert)
                                        .collect(Collectors.toList()))
                        .build();

        eventProducerServiceClient.postEventAsync(Any.pack(data));
    }
}
