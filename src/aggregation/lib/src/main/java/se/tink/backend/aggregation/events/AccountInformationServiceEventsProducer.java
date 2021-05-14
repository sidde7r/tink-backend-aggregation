package se.tink.backend.aggregation.events;

import com.google.protobuf.Any;
import java.time.Instant;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.eventproducerservice.events.grpc.AccountAggregationRestrictedEventProto;
import se.tink.eventproducerservice.events.grpc.AccountAggregationRestrictedEventProto.AccountAggregationRestrictedEvent;
import se.tink.eventproducerservice.events.grpc.AccountSourceInfoEventProto;
import se.tink.eventproducerservice.events.grpc.Psd2PaymentAccountClassificationEventProto;
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

    public void sendAccountAggregationRestrictedEvent(
            String clusterId,
            String appId,
            String userId,
            Provider provider,
            String correlationId,
            String credentialsId,
            String accountId,
            String accountType,
            String filterReason) {
        if (!eventsEnabled) {
            return;
        }
        log.info("Sending AggregationRestrictedEvent");
        try {
            AccountAggregationRestrictedEvent event =
                    AccountAggregationRestrictedEventProto.AccountAggregationRestrictedEvent
                            .newBuilder()
                            .setTimestamp(ProtobufTypeUtil.toProtobufTimestamp(Instant.now()))
                            .setMarketCode(provider.getMarket())
                            .setClusterId(clusterId)
                            .setAppId(appId)
                            .setUserId(userId)
                            .setProviderName(provider.getName())
                            .setCorrelationId(correlationId)
                            .setCredentialsId(credentialsId)
                            .setAccountId(accountId)
                            .setAccountType(accountType)
                            .setRestrictionReason(filterReason)
                            .build();
            eventProducerServiceClient.postEventFireAndForget(Any.pack(event));

        } catch (RuntimeException e) {
            log.warn(
                    "Could not produce event: AccountAggregationRestrictedEvent for (userId, credentialsId, correlationId): ({}, {}, {})",
                    userId,
                    credentialsId,
                    correlationId,
                    e);
        }
    }

    public void sendPsd2PaymentAccountClassificationEvent(
            String clusterId,
            String appId,
            String userId,
            Provider provider,
            String correlationId,
            String credentialsId,
            String accountId,
            String accountType,
            String classificationResult,
            AccountCapabilities accountCapabilities) {
        if (!eventsEnabled) {
            return;
        }
        try {
            Psd2PaymentAccountClassificationEventProto.Psd2PaymentAccountClassificationEvent event =
                    Psd2PaymentAccountClassificationEventProto.Psd2PaymentAccountClassificationEvent
                            .newBuilder()
                            .setAccountCapabilities(
                                    Psd2PaymentAccountClassificationEventProto.AccountCapabilities
                                            .newBuilder()
                                            .setCanExecuteExternalTransfer(
                                                    accountCapabilities
                                                            .getCanExecuteExternalTransfer()
                                                            .name())
                                            .setCanReceiveExternalTransfer(
                                                    accountCapabilities
                                                            .getCanReceiveExternalTransfer()
                                                            .name())
                                            .setCanPlaceFunds(
                                                    accountCapabilities.getCanPlaceFunds().name())
                                            .setCanWithdrawCash(
                                                    accountCapabilities.getCanWithdrawCash().name())
                                            .build())
                            .setAccountId(accountId)
                            .setAccountType(accountType)
                            .setAppId(appId)
                            .setUserId(userId)
                            .setClusterId(clusterId)
                            .setClassification(classificationResult)
                            .setCorrelationId(correlationId)
                            .setCredentialsId(credentialsId)
                            .setProviderName(provider.getName())
                            .setMarketCode(provider.getMarket())
                            .setProviderAccessType(provider.getAccessType().name())
                            .setProviderAuthenticationUserType(
                                    provider.getAuthenticationUserType().name())
                            .setTimestamp(ProtobufTypeUtil.toProtobufTimestamp(Instant.now()))
                            .build();

            eventProducerServiceClient.postEventFireAndForget(Any.pack(event));

        } catch (RuntimeException e) {
            log.warn(
                    "Could not produce event: Psd2PaymentAccountClassificationEvent for (userId, credentialsId, correlationId): ({}, {}, {})",
                    userId,
                    credentialsId,
                    correlationId,
                    e);
        }
    }

    public void sendAccountSourceInfoEvent(
            String clusterId,
            String appId,
            String userId,
            Provider provider,
            String correlationId,
            String credentialsId,
            String accountId,
            AccountSourceInfo sourceInfo) {
        if (!eventsEnabled) {
            return;
        }
        try {
            AccountSourceInfoEventProto.AccountSourceInfoEvent.Builder builder =
                    AccountSourceInfoEventProto.AccountSourceInfoEvent.newBuilder()
                            .setAccountId(accountId)
                            .setAppId(appId)
                            .setUserId(userId)
                            .setClusterId(clusterId)
                            .setCorrelationId(correlationId)
                            .setCredentialsId(credentialsId)
                            .setProviderName(provider.getName())
                            .setMarketCode(provider.getMarket())
                            .setTimestamp(ProtobufTypeUtil.toProtobufTimestamp(Instant.now()));
            Optional.ofNullable(sourceInfo)
                    .ifPresent(
                            info -> {
                                Optional.ofNullable(sourceInfo.getBankAccountType())
                                        .ifPresent(builder::setBankAccountType);
                                Optional.ofNullable(sourceInfo.getBankProductCode())
                                        .ifPresent(builder::setBankProductCode);
                                Optional.ofNullable(sourceInfo.getBankProductName())
                                        .ifPresent(builder::setBankProductName);
                            });
            eventProducerServiceClient.postEventFireAndForget(Any.pack(builder.build()));

        } catch (RuntimeException e) {
            log.warn(
                    "Could not produce event: AccountSourceInfoEvent for (userId, credentialsId, correlationId): ({}, {}, {})",
                    userId,
                    credentialsId,
                    correlationId,
                    e);
        }
    }
}
