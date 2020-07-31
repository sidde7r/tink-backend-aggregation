package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.loan;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.loan.rpc.FetchLoansResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class SparebankenVestLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SparebankenVestApiClient apiClient;

    private SparebankenVestLoanFetcher(SparebankenVestApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static SparebankenVestLoanFetcher create(SparebankenVestApiClient apiClient) {
        return new SparebankenVestLoanFetcher(apiClient);
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        try {
            FetchLoansResponse loansResponse = apiClient.fetchLoans();
            if (loansResponse != null) {
                return loansResponse.stream()
                        .filter(
                                loanEntity -> {
                                    if (loanEntity.isCurrencyLoan()) {
                                        fetchAndLogCurrencyLoanDetails(loanEntity);
                                        return false;
                                    }
                                    return true;
                                })
                        .map(
                                loanEntity ->
                                        loanEntity.toTinkLoan(
                                                apiClient.fetchLoanDetails(loanEntity)))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.warn("tag={}", SparebankenVestConstants.LogTags.LOANS, e);
        }

        return Collections.emptyList();
    }

    private void fetchAndLogCurrencyLoanDetails(LoanEntity loanEntity) {
        try {
            apiClient.fetchCurrencyLoanDetails(loanEntity);
        } catch (Exception e) {
            logger.info(
                    "tag={} DETAILS: Failed to fetch details",
                    SparebankenVestConstants.LogTags.LOANS,
                    e);
        }
    }
}
