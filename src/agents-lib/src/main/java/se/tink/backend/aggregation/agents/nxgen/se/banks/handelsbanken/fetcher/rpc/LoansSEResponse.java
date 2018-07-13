package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.rpc;

import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities.HandelsbankenSELoan;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.rpc.HandelsbankenLoansResponse;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.rpc.Credentials;

public class LoansSEResponse extends HandelsbankenLoansResponse {
    private List<HandelsbankenSELoan> loans;

    public List<HandelsbankenSELoan> getLoans() {
        return loans != null ? loans : Collections.emptyList();
    }

    @Override
    public Collection<LoanAccount> toTinkLoans(Credentials credentials) {
        return getLoans().stream().map(HandelsbankenSELoan::toAccount).collect(Collectors.toList());
    }
}
