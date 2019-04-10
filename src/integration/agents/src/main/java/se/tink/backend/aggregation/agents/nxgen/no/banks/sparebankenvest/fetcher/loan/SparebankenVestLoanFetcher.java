package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.loan;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.loan.rpc.FetchLoansResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SparebankenVestLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final AggregationLogger LOGGER =
            new AggregationLogger(SparebankenVestLoanFetcher.class);

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
            LOGGER.warnExtraLong(e.getMessage(), SparebankenVestConstants.LogTags.LOANS, e);
        }

        return Collections.emptyList();
    }

    private void fetchAndLogCurrencyLoanDetails(LoanEntity loanEntity) {
        try {
            LOGGER.infoExtraLong(
                    "LOAN: " + SerializationUtils.serializeToString(loanEntity),
                    SparebankenVestConstants.LogTags.LOANS);
            String loanDetails =
                    "(currency loan) " + apiClient.fetchCurrencyLoanDetails(loanEntity);
            LOGGER.infoExtraLong(
                    "LOAN DETAILS: " + loanDetails, SparebankenVestConstants.LogTags.LOANS);
        } catch (Exception e) {
            LOGGER.infoExtraLong(
                    "DETAILS: Failed to fetch details: " + e.getMessage(),
                    SparebankenVestConstants.LogTags.LOANS);
        }
    }
}
