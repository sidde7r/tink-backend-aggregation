package se.tink.libraries.transfer.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.MoreObjects;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.net.URI;
import se.tink.libraries.enums.AccountTypes;

/**
 * Fields used for creating TransferDestination: uri, name
 * Other fields for returning responses to clients.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransferDestination {
    @Tag(3)
    @ApiModelProperty(name = "balance", value="The balance of the account. Will only be populated for accounts that is owned by the user.")
    private Double balance;
    @Tag(4)
    @ApiModelProperty(name = "displayBankName", value="The name of the bank where this destination recides. Will not be populated for payment destinations.", example = "null")
    private String displayBankName;
    @Tag(8)
    @ApiModelProperty(name = "displayAccountNumber", value="A display formatted alpha-numeric string of the destination account/payment recipient number.", example = "902090-0")
    private String displayAccountNumber;
    @Tag(1)
    @ApiModelProperty(name = "uri", value="The uri used to describe this destination.", example = "se-pg://9020900")
    private URI uri;
    @Tag(2)
    @ApiModelProperty(name = "name", value="The name of the destination if one exists.", example = "Barncancerfonden")
    private String name;
    @Tag(5)
    @ApiModelProperty(name = "type", value="The account type of the destination. Will be EXTERNAL for all destinations not owned by the user.", allowableValues = AccountTypes.DOCUMENTED_TRANSFER_DESTINATION, example = "EXTERNAL")
    private String type;
    @Tag(9)
    @ApiModelProperty(name = "matchesMultiple", value="Indicates whether this TransferDestination matches multiple destinations. If true, the uri will be a regular expression, for instance \"se-pg://.+\" meaning that the source account can make PG payments.", example = "false")
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
                .toString();
    }
}
