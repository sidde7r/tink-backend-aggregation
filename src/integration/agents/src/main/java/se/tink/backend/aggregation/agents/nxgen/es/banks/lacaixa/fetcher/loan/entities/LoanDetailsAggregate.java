package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.entities;

import java.util.Arrays;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

public class LoanDetailsAggregate {
    private final LoanEntity loanEntity;
    private final LoanDetailsResponse loanDetailsResponse;

    public LoanDetailsAggregate(LoanEntity loanEntity, LoanDetailsResponse loanDetails) {
        this.loanEntity = loanEntity;
        this.loanDetailsResponse = loanDetails;
    }

    public LoanAccount toTinkLoanAccount() {

        LoanDetails loanDetails =
                LoanDetails.builder(getLoanType())
                        .setInitialBalance(
                                new Amount(
                                        loanEntity.getCurrency(),
                                        StringUtils.parseAmount(loanEntity.getTotalAmount())))
                        .setApplicants(Arrays.asList(loanDetailsResponse.getTitle()))
                        .build();
        return LoanAccount.builder(loanEntity.getContractNumber())
                .setInterestRate(
                        StringUtils.parseAmount(this.loanDetailsResponse.getNominalInterest()))
                .setAccountNumber(loanDetailsResponse.getRelatedAccountNumber())
                .setBalance(
                        new Amount(
                                        loanEntity.getCurrencyToPay(),
                                        StringUtils.parseAmount(loanEntity.getAmountToPay()))
                                .negate())
                .setName(loanEntity.getContractDescription())
                .setDetails(loanDetails)
                .build();
    }

    private LoanDetails.Type getLoanType() {
        // When we have information about more types we should investigate if we can use `productCode` instead
        if(loanEntity.getContractDescription().contains("hipotec")){
            return LoanDetails.Type.MORTGAGE;
        }

        // Set to other if we do not know the loan type
        return LoanDetails.Type.OTHER;
    }
}
