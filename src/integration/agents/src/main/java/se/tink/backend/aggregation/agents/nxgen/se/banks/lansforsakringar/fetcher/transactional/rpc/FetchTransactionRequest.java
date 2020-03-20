package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.entity.ResponseControlEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchTransactionRequest {
    private ResponseControlEntity responseControl;
    private String accountNumber;
    private String customerId;

    private FetchTransactionRequest(
            ResponseControlEntity responseControl, String accountNumber, String customerId) {
        this.responseControl = responseControl;
        this.accountNumber = accountNumber;
        this.customerId = customerId;
    }

    @JsonIgnore
    public static FetchTransactionRequest of(
            String accountNumber,
            String customerId,
            String profileType,
            String transactionStatus,
            int page) {

        ResponseControlEntity responseControlEntity =
                ResponseControlEntity.of(profileType, customerId, transactionStatus, page);

        return new FetchTransactionRequest(responseControlEntity, accountNumber, customerId);
    }
}
