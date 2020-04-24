package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.loan.entities;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.OmaspConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.entities.DateEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class LoanDetailsEntity extends LoanEntity {
    private String interestType;
    private String interestRate;
    private DateEntity nextInterestAdjustmentDate;
    private String invoicingMethod;
    private String chargeAccountIban;

    public Double getInterestRate() {
        if (!Strings.isNullOrEmpty(interestRate)) {
            return AgentParsingUtils.parsePercentageFormInterest(interestRate);
        }
        return null;
    }

    public boolean isKnownLoanType() {
        return OmaspConstants.LOAN_TYPES.containsKey(loanCategory.toLowerCase());
    }

    public LoanDetails.Type getLoanType() {
        return OmaspConstants.LOAN_TYPES.getOrDefault(
                loanCategory.toLowerCase(), LoanDetails.Type.OTHER);
    }

    public LoanAccount toTinkAccount() {
        return LoanAccount.builder(id, ExactCurrencyAmount.inEUR(balance.getValue()))
                .setAccountNumber(loanNumber)
                .setInterestRate(getInterestRate())
                .setBankIdentifier(id)
                .setName(getName())
                .setDetails(LoanDetails.builder(getLoanType()).setLoanNumber(loanNumber).build())
                .build();
    }
}
