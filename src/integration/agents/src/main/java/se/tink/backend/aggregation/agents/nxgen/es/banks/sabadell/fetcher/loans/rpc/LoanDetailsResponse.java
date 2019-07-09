package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.loans.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.loans.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.loans.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.loans.entities.LoanLineEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@JsonObject
public class LoanDetailsResponse {
    private final Logger log = LoggerFactory.getLogger(LoanDetailsResponse.class);

    private String aIndamor;
    private LoanLineEntity loanLine;
    private List<LoanEntity> loans;

    @JsonIgnore
    public LoanAccount toTinkLoanAccount() {
        if (loans.size() > 1) {
            log.error("Account has more than 1 loan. Will only parse 1 loan.");
        }
        LoanEntity loan = loans.get(0);
        AccountEntity account = loanLine.getAccount();

        return LoanAccount.builder(
                        account.getContractNumberFormatted(),
                        loan.getBalance().stripSign().negate())
                .setName(account.getDescription())
                .setAccountNumber(account.getContractNumberFormatted())
                .addIdentifier(new IbanIdentifier(account.getBic(), account.getIban()))
                .setInterestRate(loan.getInterest())
                .setDetails(getLoanDetails(loan))
                .setHolderName(account.getHolder())
                .build();
    }

    @JsonIgnore
    private LoanDetails getLoanDetails(LoanEntity loan) {
        List<String> applicants = Arrays.asList(loanLine.getOwner());
        return LoanDetails.builder(LoanDetails.Type.DERIVE_FROM_NAME)
                .setAmortized(loan.getAmortized())
                .setApplicants(applicants)
                .setCoApplicant(applicants.size() > 1)
                .setInitialBalance(loan.getInitialBalance())
                .setLoanNumber(loanLine.getContract())
                .setMonthlyAmortization(loan.getInstalmentValue())
                .build();
    }

    @JsonIgnore
    public boolean hasLoans() {
        return loans.size() > 0;
    }
}
