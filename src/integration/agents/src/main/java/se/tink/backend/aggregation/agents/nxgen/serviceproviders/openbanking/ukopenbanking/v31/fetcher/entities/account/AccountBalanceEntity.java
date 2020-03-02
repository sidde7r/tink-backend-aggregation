package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.ExternalLimitType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.entities.CreditLineEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Data
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class AccountBalanceEntity {
    private String accountId;

    private AmountEntity amount;

    private UkOpenBankingApiDefinitions.CreditDebitIndicator creditDebitIndicator;

    private UkOpenBankingApiDefinitions.AccountBalanceType type;

    private String dateTime;

    private List<CreditLineEntity> creditLine;

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
        return amount;
    }

    public Optional<ExactCurrencyAmount> getAvailableCredit() {
        return ExternalLimitType.getPreferredCreditLineEntity(creditLine)
                .map(CreditLineEntity::getAmount);
    }

    private ExactCurrencyAmount getSignedAmount() {
        // Remove sign included in balance value
        ExactCurrencyAmount unsignedAmount = amount.abs();

        // Apply sign based on credit/debit indicator
        return creditDebitIndicator == UkOpenBankingApiDefinitions.CreditDebitIndicator.CREDIT
                ? unsignedAmount
                : unsignedAmount.negate();
    }
}
