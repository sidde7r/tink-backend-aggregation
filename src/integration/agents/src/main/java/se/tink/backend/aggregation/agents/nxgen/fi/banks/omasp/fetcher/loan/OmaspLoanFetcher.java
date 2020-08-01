package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.loan;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.OmaspApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.OmaspConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class OmaspLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final OmaspApiClient apiClient;

    public OmaspLoanFetcher(OmaspApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        List<LoanEntity> loans = apiClient.getLoans();

        return loans.stream()
                .map(
                        loan -> {
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
        logger.info(
                "tag={} {}",
                OmaspConstants.LogTags.LOG_TAG_LOAN_DETAILS,
                SerializationUtils.serializeToString(loanDetails));
    }
}
