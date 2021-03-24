package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class ListAccountTransactionRequest {
    private ResponseControlEntity responseControl;
    private String accountNumber;
    private String customerId;

    @JsonIgnore
    public static ListAccountTransactionRequest of(
            String accountNumber,
            String customerId,
            String profileType,
            String transactionStatus,
            int page) {

        return new ListAccountTransactionRequest(
                ResponseControlEntity.of(profileType, customerId, transactionStatus, page),
                accountNumber,
                customerId);
    }
}
