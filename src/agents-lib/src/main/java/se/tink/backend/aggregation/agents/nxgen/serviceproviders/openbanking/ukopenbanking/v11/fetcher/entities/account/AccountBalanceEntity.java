package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.fetcher.entities.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.core.Amount;

@JsonObject
public class AccountBalanceEntity {

    @JsonProperty("AccountId")
    private String accountId;
    @JsonProperty("Amount")
    private AmountEntity balance;
    @JsonProperty("CreditDebitIndicator")
    private UkOpenBankingConstants.CreditDebitIndicator creditDebitIndicator;
    @JsonProperty("Type")
    private UkOpenBankingConstants.AccountBalanceType type;
    @JsonProperty("DateTime")
    private String dateTime;
    @JsonProperty("CreditLine")
    private List<CreditLineEntity> creditLine;

    public String getAccountId() {
        return accountId;
    }

    public Amount getBalance() {

        Amount total = getSignedAmount();

        if (creditLine != null) {
            for (CreditLineEntity credit : creditLine) {
                if (credit.isIncluded()) {
                    total = total.subtract(credit.getAmount());
                }
            }
        }

        return total;
    }

    public Optional<Amount> getAvaliableCredit() {

        if (creditLine == null || creditLine.isEmpty()) {
            return Optional.empty();
        }

        Amount total = new Amount(balance.getCurrency(), 0D);
        for (CreditLineEntity credit : creditLine) {
            if (credit.isIncluded()) {
                total = total.add(credit.getAmount());
            }
        }

        return Optional.of(total);
    }

    private Amount getSignedAmount() {
        // Remove sign included in balance value
        Amount unsignedAmount = balance.stripSign();

        // Apply sign based on credit/debit indicator
        return creditDebitIndicator == UkOpenBankingConstants.CreditDebitIndicator.CREDIT ?
                unsignedAmount :
                unsignedAmount.negate();
    }

    public UkOpenBankingConstants.AccountBalanceType getType() {
        return type;
    }
}
