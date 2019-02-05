package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;

@JsonObject
public class SdcLoanAccount {
    private String loanType;
    private String label;
    private String secondaryLabel;
    private SdcAmount amount;
    private String secondayAmount;
    // `labelValuePairList` is null - cannot define it!
    private SdcAccountKey entityKey;

    public String getLoanType() {
        return loanType;
    }

    public String getLabel() {
        return label;
    }

    public String getSecondaryLabel() {
        return secondaryLabel;
    }

    public SdcAmount getAmount() {
        return amount;
    }

    public String getSecondayAmount() {
        return secondayAmount;
    }

    public Optional<String> findAccountId() {
        return Optional.ofNullable(entityKey)
                .map(SdcAccountKey::getAccountId);
    }

    public LoanAccount toTinkLoan(String defaultCurrency) {
        // No interest rate returned from the bank
        return LoanAccount.builder(getSecondaryLabel(), amount.toTinkAmount(defaultCurrency))
                .setAccountNumber(findAccountId().orElse(getSecondaryLabel()))
                .setName(getLabel())
                .setBankIdentifier(getSecondaryLabel())
                .build();
    }
}
