package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

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
        return Optional.ofNullable(entityKey).map(SdcAccountKey::getAccountId);
    }

    public LoanAccount toTinkLoan(String defaultCurrency) {
        // No interest rate returned from the bank
        return LoanAccount.builder(
                        getSecondaryLabel(), amount.toExactCurrencyAmount(defaultCurrency))
                .setAccountNumber(findAccountId().orElse(getSecondaryLabel()))
                .setName(getLabel())
                .setBankIdentifier(getSecondaryLabel())
                // We don't have any "properties" or "permissions" to look at for LoanAccounts.
                // Instead we assume that we do not have any of the capabilities.
                // Note: The capabilities are meant to signify "instant/direct" result, you can
                // normally not place/withdraw funds directly to/from a loan account.
                .canWithdrawFunds(AccountCapabilities.Answer.NO)
                .canPlaceFunds(AccountCapabilities.Answer.NO)
                .canReceiveDomesticTransfer(AccountCapabilities.Answer.NO)
                .canMakeDomesticTransfer(AccountCapabilities.Answer.NO)
                .build();
    }
}
