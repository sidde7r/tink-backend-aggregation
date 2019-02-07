package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankCreditEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.amount.Amount;

@JsonObject
public class CreditDetailsResponse {
    private double totalInterestRate;
    private double creditLimit;
    private double balance;
    private String interestBound;
    private double interestMarginal;
    private int state;

    @JsonIgnore
    public LoanAccount toLoanAccount(OpBankCreditEntity creditEntity) {
        return LoanAccount.builder(creditEntity.getAgreementNumberIban(), Amount.inEUR(balance))
                .setAccountNumber(creditEntity.getAgreementNumberIban())
                .setInterestRate(totalInterestRate)
                .setBankIdentifier(creditEntity.getAgreementNumberIban())
                .setName(creditEntity.getLoanName())
                .setDetails(LoanDetails.builder(OpBankConstants.LoanType.findLoanType(creditEntity.getUsage()).getTinkType())
                        .setLoanNumber(creditEntity.getAgreementNumberIban())
                        .build()
                )
                .build();
    }
}
