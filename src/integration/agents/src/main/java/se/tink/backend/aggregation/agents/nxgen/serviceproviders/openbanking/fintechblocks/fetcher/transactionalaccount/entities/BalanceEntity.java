package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksConstants.BalanceTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceEntity {

    public static final Amount DEFAULT = new Amount("HUF", 0);

    @JsonProperty("AccountId")
    private String accountId;

    @JsonProperty("Amount")
    private AmountEntity amount;

    @JsonProperty("CreditDebitIndicator")
    private String creditDebitIndicator;

    @JsonProperty("DateTime")
    private String dateTime;

    @JsonProperty("Type")
    private String type;

    public Amount getAmount() {
        return amount.toAmount();
    }

    public boolean isAvailableBalance() {
        return type.equalsIgnoreCase(BalanceTypes.CLOSING_AVAILABLE)
                || type.equalsIgnoreCase(BalanceTypes.FORWARD_AVAILABLE)
                || type.equalsIgnoreCase(BalanceTypes.INTERIM_AVAILABLE);
    }
}
