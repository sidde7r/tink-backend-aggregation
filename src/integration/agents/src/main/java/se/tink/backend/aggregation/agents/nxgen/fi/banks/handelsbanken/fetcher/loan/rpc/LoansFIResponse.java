package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.loan.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.loan.entities.HandelsbankenFILoan;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.loan.rpc.HandelsbankenLoansResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LoansFIResponse extends HandelsbankenLoansResponse {

    private boolean displayBadge; // Same type as in app v2208
    private String emptyDisclaimer; // Same type as in app v2208
    private List<HandelsbankenFILoan> loans;
    private Object
            totalLoanAmountList; // List<com.handelsbanken.android.resources.domain.AmountDTO> In
    // app v2208
    private Object specialTextList; // List<java.lang.String> In app v2208
    private Object
            totalLoanPaymentsAmountList; // List<com.handelsbanken.android.resources.domain.AmountDTO> In app 2208

    @Override
    public Collection<LoanAccount> toTinkLoans(Credentials credentials) {
        Collection<LoanAccount> loanAccounts = Collections.EMPTY_LIST;

        if (this.loans != null) {
            loanAccounts =
                    this.loans.stream()
                            .map(HandelsbankenFILoan::toLoanAccount)
                            .collect(Collectors.toList());
        }

        return loanAccounts;
    }

    @Override
    public String toString() {
        return SerializationUtils.serializeToString(this);
    }
}
