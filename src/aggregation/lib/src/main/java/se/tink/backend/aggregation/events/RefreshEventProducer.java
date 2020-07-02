package se.tink.backend.aggregation.events;

import com.google.protobuf.Any;
import java.time.Instant;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.eventproducerservice.events.grpc.RefreshResultEventProto.RefreshResultEvent;
import se.tink.eventproducerservice.events.grpc.RefreshResultEventProto.RefreshResultEvent.AdditionalInfo;
import se.tink.eventproducerservice.events.grpc.RefreshResultEventProto.RefreshResultEvent.RefreshResult;
import se.tink.eventproducerservice.events.grpc.RefreshResultEventProto.RefreshResultEvent.RefreshableItem;
import se.tink.libraries.event_producer_service_client.grpc.EventProducerServiceClient;
import se.tink.libraries.serialization.proto.utils.ProtobufTypeUtil;

public class RefreshEventProducer {

    private static final Logger log = LoggerFactory.getLogger(RefreshEventProducer.class);
    private final EventProducerServiceClient eventProducerServiceClient;
    private final boolean sendAgentRefreshEventsEnabled;

    @Inject
    public RefreshEventProducer(
            EventProducerServiceClient eventProducerServiceClient,
            @Named("sendAgentRefreshEvents") boolean sendAgentRefreshEventsEnabled) {
        this.eventProducerServiceClient = eventProducerServiceClient;
        this.sendAgentRefreshEventsEnabled = sendAgentRefreshEventsEnabled;
    }

    public void sendEventForRefreshWithUnknownError(
            String providerName,
            String correlationId,
            String marketCode,
            String credentialsId,
            String appId,
            String clusterId,
            String userId,
            se.tink.libraries.credentials.service.RefreshableItem refreshableItem) {
        sendRefreshEvent(
                providerName,
                correlationId,
                marketCode,
                credentialsId,
                appId,
                clusterId,
                userId,
                RefreshResult.UNKNOWN_ERROR,
                AdditionalInfo.ERROR_INFO,
                refreshableItem);
    }

    public void sendEventForRefreshWithErrorInTinkSide(
            String providerName,
            String correlationId,
            String marketCode,
            String credentialsId,
            String appId,
            String clusterId,
            String userId,
            se.tink.libraries.credentials.service.RefreshableItem refreshableItem) {
        sendRefreshEvent(
                providerName,
                correlationId,
                marketCode,
                credentialsId,
                appId,
                clusterId,
                userId,
                RefreshResult.TINK_ERROR,
                AdditionalInfo.ERROR_INFO,
                refreshableItem);
    }

    public void sendEventForRefreshWithErrorInTinkSide(
            String providerName,
            String correlationId,
            String marketCode,
            String credentialsId,
            String appId,
            String clusterId,
            String userId,
            AdditionalInfo additionalInfo,
            se.tink.libraries.credentials.service.RefreshableItem refreshableItem) {
        sendRefreshEvent(
                providerName,
                correlationId,
                marketCode,
                credentialsId,
                appId,
                clusterId,
                userId,
                RefreshResult.TINK_ERROR,
                additionalInfo,
                refreshableItem);
    }

    public void sendEventForRefreshWithErrorInBankSide(
            String providerName,
            String correlationId,
            String marketCode,
            String credentialsId,
            String appId,
            String clusterId,
            String userId,
            se.tink.libraries.credentials.service.RefreshableItem refreshableItem) {
        sendRefreshEvent(
                providerName,
                correlationId,
                marketCode,
                credentialsId,
                appId,
                clusterId,
                userId,
                RefreshResult.BANK_ERROR,
                AdditionalInfo.ERROR_INFO,
                refreshableItem);
    }

    public void sendEventForRefreshWithErrorInBankSide(
            String providerName,
            String correlationId,
            String marketCode,
            String credentialsId,
            String appId,
            String clusterId,
            String userId,
            AdditionalInfo additionalInfo,
            se.tink.libraries.credentials.service.RefreshableItem refreshableItem) {
        sendRefreshEvent(
                providerName,
                correlationId,
                marketCode,
                credentialsId,
                appId,
                clusterId,
                userId,
                RefreshResult.BANK_ERROR,
                additionalInfo,
                refreshableItem);
    }

    private void sendRefreshEvent(
            String providerName,
            String correlationId,
            String marketCode,
            String credentialsId,
            String appId,
            String clusterId,
            String userId,
            RefreshResult refreshResult,
            AdditionalInfo additionalInfo,
            se.tink.libraries.credentials.service.RefreshableItem refreshableItem) {

        if (!sendAgentRefreshEventsEnabled) {
            return;
        }

        try {
            RefreshableItem item;
            switch (refreshableItem) {
                case EINVOICES:
                    item = RefreshableItem.EINVOICES;
                    break;
                case TRANSFER_DESTINATIONS:
                    item = RefreshableItem.TRANSFER_DESTINATIONS;
                    break;
                case CHECKING_ACCOUNTS:
                    item = RefreshableItem.CHECKING_ACCOUNTS;
                    break;
                case CHECKING_TRANSACTIONS:
                    item = RefreshableItem.CHECKING_TRANSACTIONS;
                    break;
                case SAVING_ACCOUNTS:
                    item = RefreshableItem.SAVING_ACCOUNTS;
                    break;
                case SAVING_TRANSACTIONS:
                    item = RefreshableItem.SAVING_TRANSACTIONS;
                    break;
                case CREDITCARD_ACCOUNTS:
                    item = RefreshableItem.CREDITCARD_ACCOUNTS;
                    break;
                case CREDITCARD_TRANSACTIONS:
                    item = RefreshableItem.CREDITCARD_TRANSACTIONS;
                    break;
                case LOAN_ACCOUNTS:
                    item = RefreshableItem.LOAN_ACCOUNTS;
                    break;
                case LOAN_TRANSACTIONS:
                    item = RefreshableItem.LOAN_TRANSACTIONS;
                    break;
                case INVESTMENT_ACCOUNTS:
                    item = RefreshableItem.INVESTMENT_ACCOUNTS;
                    break;
                case INVESTMENT_TRANSACTIONS:
                    item = RefreshableItem.INVESTMENT_TRANSACTIONS;
                    break;
                case IDENTITY_DATA:
                    item = RefreshableItem.IDENTITY_DATA;
                    break;
                default:
                    throw new Exception(
                            String.format(
                                    "Refreshable item %s could not be handled by RefreshEventProducer",
                                    refreshableItem.toString()));
            }

            RefreshResultEvent event =
                    RefreshResultEvent.newBuilder()
                            .setTimestamp(ProtobufTypeUtil.toProtobufTimestamp(Instant.now()))
                            .setProviderName(providerName)
                            .setCorrelationId(correlationId)
                            .setMarketCode(marketCode)
                            .setAppId(appId)
                            .setClusterId(clusterId)
                            .setUserId(userId)
                            .setCredentialsId(credentialsId)
                            .setRefreshResult(refreshResult)
                            .setAdditionalInfo(additionalInfo)
                            .setRefreshableItem(item)
                            .build();

            eventProducerServiceClient.postEventAsync(Any.pack(event));
        } catch (Exception e) {
            log.error("Could not push agent refresh event", e);
        }
    }
}
