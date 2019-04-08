package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DataRequestEntity {
    @JsonProperty("Permissions")
    private List<String> permissions;

    @JsonProperty("TransactionFromDate")
    private String transactionFromDateString;

    @JsonProperty("TransactionToDate")
    private String transactionToDateString;

    @JsonProperty("ExpirationDate")
    private String expirationDateString;

    public List<String> getPermissions() {
        return permissions;
    }

    public String getTransactionFromDateString() {
        return transactionFromDateString;
    }

    public String getTransactionToDateString() {
        return transactionToDateString;
    }

    public String getExpirationDateString() {
        return expirationDateString;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public void setTransactionFromDateString(String transactionFromDateString) {
        this.transactionFromDateString = transactionFromDateString;
    }

    public void setTransactionToDateString(String transactionToDateString) {
        this.transactionToDateString = transactionToDateString;
    }

    public void setExpirationDateString(String expirationDateString) {
        this.expirationDateString = expirationDateString;
    }
}
