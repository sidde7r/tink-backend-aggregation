package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountBalanceEntity {
    @JsonProperty("AccountId")
    private String accountId;

    @JsonProperty("Amount")
    private AmountEntity balance;

    @JsonProperty("CreditDebitIndicator")
    private UkOpenBankingApiDefinitions.CreditDebitIndicator creditDebitIndicator;

    @JsonProperty("Type")
    private UkOpenBankingApiDefinitions.AccountBalanceType type;

    @JsonProperty("DateTime")
    private String dateTime;

    @JsonProperty("CreditLine")
    private List<CreditLineEntity> creditLine;

    public String getAccountId() {
        return accountId;
    }

    public Amount getBalance() {

        Amount total = getSignedAmount();

        // If no credit line is present the balance is already calculated.
        if (creditLine == null || creditLine.isEmpty()) {
            return total;
        }

        // Only one credit line can be approved at a time, but this can be repeated under a
        // different ExternalLimitType.
        // We find the first credit line that is included in the balance and return (balance -
        // credit).
        // ExternalLimitType.AVAILABLE is not useful when calculating credit exclusive balance so
        // this is ignored.
        for (CreditLineEntity credit : creditLine) {
            if (credit.getType() != UkOpenBankingApiDefinitions.ExternalLimitType.AVAILABLE) {
                if (credit.isIncluded()) {
                    return total.subtract(credit.getAmount());
                }
            }
        }

        return total;
    }

    public Optional<Amount> getAvailableCredit() {

        if (creditLine == null || creditLine.isEmpty()) {
            return Optional.empty();
        }

        return creditLine
                .stream()
                .filter(
                        credit ->
                                credit.getType()
                                        == UkOpenBankingApiDefinitions.ExternalLimitType.AVAILABLE)
                .findAny()
                .map(CreditLineEntity::getAmount);
    }

    private Amount getSignedAmount() {
        // Remove sign included in balance value
        Amount unsignedAmount = balance.stripSign();

        // Apply sign based on credit/debit indicator
        return creditDebitIndicator == UkOpenBankingApiDefinitions.CreditDebitIndicator.CREDIT
                ? unsignedAmount
                : unsignedAmount.negate();
    }

    public UkOpenBankingApiDefinitions.AccountBalanceType getType() {
        return type;
    }
}
