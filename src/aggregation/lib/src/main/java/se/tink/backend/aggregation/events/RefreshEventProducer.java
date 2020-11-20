package se.tink.backend.aggregation.events;

import com.google.protobuf.Any;
import java.time.Instant;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;
import se.tink.eventproducerservice.events.grpc.RefreshResultEventProto.RefreshResultEvent;
import se.tink.eventproducerservice.events.grpc.RefreshResultEventProto.RefreshResultEvent.RefreshResult;
import se.tink.eventproducerservice.events.grpc.RefreshResultEventProto.RefreshResultEvent.RefreshableItem;
import se.tink.libraries.event_producer_service_client.grpc.EventProducerServiceClient;
import se.tink.libraries.serialization.proto.utils.ProtobufTypeUtil;

@Slf4j
public class RefreshEventProducer {
    private final EventProducerServiceClient eventProducerServiceClient;
    private final boolean sendAgentRefreshEventsEnabled;

    @Inject
    public RefreshEventProducer(
            EventProducerServiceClient eventProducerServiceClient,
            @Named("sendAgentRefreshEvents") boolean sendAgentRefreshEventsEnabled) {
        this.eventProducerServiceClient = eventProducerServiceClient;
        this.sendAgentRefreshEventsEnabled = sendAgentRefreshEventsEnabled;
    }

    public void sendEventForRefreshWithErrorInTinkSide(RefreshEvent refreshEvent) {
        sendRefreshEvent(refreshEvent, RefreshResult.TINK_ERROR);
    }

    public void sendEventForRefreshWithErrorInBankSide(RefreshEvent refreshEvent) {
        sendRefreshEvent(refreshEvent, RefreshResult.BANK_ERROR);
    }

    private void sendRefreshEvent(RefreshEvent refreshEvent, RefreshResult refreshResult) {

        if (!sendAgentRefreshEventsEnabled) {
            return;
        }

        try {
            RefreshableItem grpcRefreshableItem;
            se.tink.libraries.credentials.service.RefreshableItem credentialsRefreshableItem =
                    refreshEvent.getRefreshableItem();
            switch (credentialsRefreshableItem) {
                case EINVOICES:
                    grpcRefreshableItem = RefreshableItem.EINVOICES;
                    break;
                case TRANSFER_DESTINATIONS:
                    grpcRefreshableItem = RefreshableItem.TRANSFER_DESTINATIONS;
                    break;
                case CHECKING_ACCOUNTS:
                    grpcRefreshableItem = RefreshableItem.CHECKING_ACCOUNTS;
                    break;
                case CHECKING_TRANSACTIONS:
                    grpcRefreshableItem = RefreshableItem.CHECKING_TRANSACTIONS;
                    break;
                case SAVING_ACCOUNTS:
                    grpcRefreshableItem = RefreshableItem.SAVING_ACCOUNTS;
                    break;
                case SAVING_TRANSACTIONS:
                    grpcRefreshableItem = RefreshableItem.SAVING_TRANSACTIONS;
                    break;
                case CREDITCARD_ACCOUNTS:
                    grpcRefreshableItem = RefreshableItem.CREDITCARD_ACCOUNTS;
                    break;
                case CREDITCARD_TRANSACTIONS:
                    grpcRefreshableItem = RefreshableItem.CREDITCARD_TRANSACTIONS;
                    break;
                case LOAN_ACCOUNTS:
                    grpcRefreshableItem = RefreshableItem.LOAN_ACCOUNTS;
                    break;
                case LOAN_TRANSACTIONS:
                    grpcRefreshableItem = RefreshableItem.LOAN_TRANSACTIONS;
                    break;
                case INVESTMENT_ACCOUNTS:
                    grpcRefreshableItem = RefreshableItem.INVESTMENT_ACCOUNTS;
                    break;
                case INVESTMENT_TRANSACTIONS:
                    grpcRefreshableItem = RefreshableItem.INVESTMENT_TRANSACTIONS;
                    break;
                case IDENTITY_DATA:
                    grpcRefreshableItem = RefreshableItem.IDENTITY_DATA;
                    break;
                case LIST_BENEFICIARIES:
                    grpcRefreshableItem = RefreshableItem.LIST_BENEFICIARIES;
                    break;
                default:
                    throw new Exception(
                            String.format(
                                    "Refreshable grpcRefreshableItem %s could not be handled by RefreshEventProducer",
                                    credentialsRefreshableItem.toString()));
            }

            RefreshResultEvent event =
                    RefreshResultEvent.newBuilder()
                            .setTimestamp(ProtobufTypeUtil.toProtobufTimestamp(Instant.now()))
                            .setProviderName(refreshEvent.getProviderName())
                            .setCorrelationId(refreshEvent.getCorrelationId())
                            .setMarketCode(refreshEvent.getMarketCode())
                            .setAppId(refreshEvent.getAppId())
                            .setClusterId(refreshEvent.getClusterId())
                            .setUserId(refreshEvent.getUserId())
                            .setCredentialsId(refreshEvent.getCredentialsId())
                            .setRefreshResult(refreshResult)
                            .setAdditionalInfo(refreshEvent.getAdditionalInfo())
                            .setRefreshableItem(grpcRefreshableItem)
                            .build();

            eventProducerServiceClient.postEventAsync(Any.pack(event));
        } catch (Exception e) {
            log.error("Could not push agent refresh event", e);
        }
    }
}
