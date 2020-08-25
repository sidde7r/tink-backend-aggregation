package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GroupHeaderEntity {
    @JsonProperty("http_code")
    private int httpCode;

    @JsonProperty("message_identification")
    private String messageIdentification;

    @JsonProperty("creation_date_time")
    private String creationDateTime;

    @JsonProperty("message_pagination")
    private TransactionPaginationEntity transactionPagination;

    public int getHttpCode() {
        return httpCode;
    }

    public String getMessageIdentification() {
        return messageIdentification;
    }

    public String getCreationDateTime() {
        return creationDateTime;
    }

    public TransactionPaginationEntity getTransactionPagination() {
        return transactionPagination;
    }
}
