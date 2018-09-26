package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Dictionary;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DataResponseEntity {
    @JsonProperty("AccountRequestId")
    private String accountRequestId;
    @JsonProperty("Status")
    private String status;
    @JsonProperty("CreationDateTime")
    private Date creationDateTime;
    @JsonProperty("Permissions")
    private List<String> permissions;
    @JsonProperty("TransactionFromDate")
    private Date transactionFromDate;
    @JsonProperty("TransactionToDate")
    private Date transactionToDate;
    @JsonProperty("ExpirationDate")
    private Date expirationDate;

    public String getAccountRequestId() {
        return accountRequestId;
    }

    public String getStatus() {
        return status;
    }

    public Date getCreationDateTime() {
        return creationDateTime;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public Date getTransactionFromDate() {
        return transactionFromDate;
    }

    public Date getTransactionToDate() {
        return transactionToDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }
}
