package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.loan;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.OmaspApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.OmaspConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class OmaspLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final AggregationLogger log = new AggregationLogger(OmaspLoanFetcher.class);

    private final OmaspApiClient apiClient;

    public OmaspLoanFetcher(OmaspApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        List<LoanEntity> loans = apiClient.getLoans();

        return loans.stream()
                .map(loan -> {
                    LoanDetailsEntity loanDetails = apiClient.getLoanDetails(loan.getId());
                    if (!loanDetails.isKnownLoanType()) {
                        logLoanDetails(loanDetails);
                    }
                    return loanDetails;
                })
                .map(LoanDetailsEntity::toTinkAccount)
                .collect(Collectors.toList());
    }

    private void logLoanDetails(LoanDetailsEntity loanDetails) {
        log.infoExtraLong(SerializationUtils.serializeToString(loanDetails),
                OmaspConstants.LogTags.LOG_TAG_LOAN_DETAILS);
    }
}
