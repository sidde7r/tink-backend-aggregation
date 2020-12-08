package se.tink.libraries.transfer.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.MoreObjects;
import java.net.URI;

/**
 * Fields used for creating TransferDestination: uri, name Other fields for returning responses to
 * clients.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransferDestination {
    private Double balance;
    private String displayBankName;
    private String displayAccountNumber;
    private URI uri;
    private String name;
    private String type;
    private boolean matchesMultiple;

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getDisplayBankName() {
        return displayBankName;
    }

    public void setDisplayBankName(String displayBankName) {
        this.displayBankName = displayBankName;
    }

    public String getDisplayAccountNumber() {
        return displayAccountNumber;
    }

    public void setDisplayAccountNumber(String displayAccountNumber) {
        this.displayAccountNumber = displayAccountNumber;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isMatchesMultiple() {
        return matchesMultiple;
    }

    public void setMatchesMultiple(boolean matchesMultiple) {
        this.matchesMultiple = matchesMultiple;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("uri", uri)
                .add("name", name)
                .add("balance", balance)
                .add("matchesMultiple", matchesMultiple)
                .add("displayBankName", displayBankName)
                .add("displayAccountNumber", displayAccountNumber == null ? null : "***")
                .add("type", type)
                .toString();
    }
}
