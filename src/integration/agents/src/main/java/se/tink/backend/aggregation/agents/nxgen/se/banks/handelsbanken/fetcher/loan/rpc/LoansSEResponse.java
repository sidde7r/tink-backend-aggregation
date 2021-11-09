package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.loan.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.loan.entities.HandelsbankenSELoan;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.loan.entities.SELoanInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.loan.rpc.HandelsbankenLoansResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

@Slf4j
public class LoansSEResponse extends HandelsbankenLoansResponse {
    private List<HandelsbankenSELoan> loans;
    private SELoanInfoEntity loanInfo;

    public List<HandelsbankenSELoan> getLoans() {
        if (loans != null) {
            log.info("Handelsbanken returned an old version of the loan response.");
            return loans;
        }

        if (loanInfo != null) {
            log.info("Handelsbanken returned a new version of the loan response.");
            return loanInfo.getLoans();
        }

        return Collections.emptyList();
    }

    @Override
    public Collection<LoanAccount> toTinkLoans(Credentials credentials) {
        return getLoans().stream().map(HandelsbankenSELoan::toAccount).collect(Collectors.toList());
    }
}
