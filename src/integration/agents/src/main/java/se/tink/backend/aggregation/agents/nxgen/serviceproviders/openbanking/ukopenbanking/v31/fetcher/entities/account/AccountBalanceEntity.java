package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.ExternalLimitType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.entities.CreditLineEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

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

    public ExactCurrencyAmount calculateAccountSpecificBalance() {

        ExactCurrencyAmount total = getSignedAmount();

        // If no credit line is present the balance is already calculated.
        return Optional.ofNullable(creditLine).orElseGet(Collections::emptyList).stream()
                // Only one credit line can be approved at a time, but this can be repeated under a
                // different ExternalLimitType.
                // We find the first credit line that is included in the balance and return (balance
                // -
                // credit).
                // ExternalLimitType.AVAILABLE is not useful when calculating credit exclusive
                // balance so
                // this is ignored.
                .filter(
                        credit ->
                                credit.getType()
                                        != UkOpenBankingApiDefinitions.ExternalLimitType.AVAILABLE)
                .filter(CreditLineEntity::isIncluded)
                .map(credit -> total.subtract(credit.getAmount()))
                .findFirst()
                .orElse(total);
    }

    public ExactCurrencyAmount getAsCurrencyAmount() {
        return balance;
    }

    public Optional<ExactCurrencyAmount> getAvailableCredit() {
        return ExternalLimitType.getPreferredCreditLineEntity(creditLine)
                .map(CreditLineEntity::getAmount);
    }

    private ExactCurrencyAmount getSignedAmount() {
        // Remove sign included in balance value
        ExactCurrencyAmount unsignedAmount = balance.abs();

        // Apply sign based on credit/debit indicator
        return creditDebitIndicator == UkOpenBankingApiDefinitions.CreditDebitIndicator.CREDIT
                ? unsignedAmount
                : unsignedAmount.negate();
    }

    public UkOpenBankingApiDefinitions.AccountBalanceType getType() {
        return type;
    }

    public List<CreditLineEntity> getCreditLine() {
        return creditLine;
    }
}
