package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.Accounts;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardsEntity {
    private String cardName;
    private double balance;
    private String cardNumber;
    private String status;
    private String cardTypeAsString;
    private String connectedAccountNumber;
    private double cardLimit;
    private double cardAvailable;
    private String expires;
    private double reservedAmount;
    private double aviAmount;
    private String versionNumber;
    private String statusText;
    private boolean replaced;
    private CardAccountDetailsEntity cardAccountDetails;
    private boolean accountDetailsAvailable;

    @JsonIgnore
    public boolean isNotDebit() {
        return !Accounts.ACCOUNT_TYPE_MAPPER.isOf(cardTypeAsString, AccountTypes.CHECKING);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("cardName", cardName)
                .add("balance", balance)
                .add("cardNumber", cardNumber)
                .add("status", status)
                .add("cardTypeAsString", cardTypeAsString)
                .add("ConnectedAccountNumber", connectedAccountNumber)
                .add("cardLimit", cardLimit)
                .add("cardAvailable", cardAvailable)
                .add("expires", expires)
                .add("reservedAmount", reservedAmount)
                .add("aviAmount", aviAmount)
                .add("versionNumber", versionNumber)
                .add("statusText", statusText)
                .add("replaced", replaced)
                .add("cardAccountDetails", cardAccountDetails)
                .toString();
    }
}
