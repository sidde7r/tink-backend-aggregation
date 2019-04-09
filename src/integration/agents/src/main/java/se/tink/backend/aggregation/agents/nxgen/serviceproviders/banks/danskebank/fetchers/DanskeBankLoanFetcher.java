package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListLoansRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListLoansResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.LoanDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class DanskeBankLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final AggregationLogger LOG = new AggregationLogger(DanskeBankLoanFetcher.class);

    private final Credentials credentials;
    private final DanskeBankApiClient apiClient;
    private final DanskeBankConfiguration configuration;
    private final String languageCode;

    public DanskeBankLoanFetcher(
            Credentials credentials,
            DanskeBankApiClient apiClient,
            DanskeBankConfiguration configuration) {
        this.credentials = credentials;
        this.apiClient = apiClient;
        this.configuration = configuration;
        this.languageCode = configuration.getLanguageCode();
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {

        // just to not mess with the entire refresh, until we can verify
        // this actually works, return empty if exception
        // TODO: remove try/catch once we see it works
        try {
            // this is only mortgages (real estate)
            ListLoansResponse loansResponse =
                    apiClient.listLoans(ListLoansRequest.createFromLanguageCode(languageCode));

            return Optional.ofNullable(loansResponse.getLoans()).orElse(Collections.emptyList())
                    .stream()
                    .map(this::toLoanAccount)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.warn(
                    DanskeBankConstants.LogTags.LOAN_ACCOUNT
                            + " - Failed to fetch loans "
                            + e.getMessage());

            return Collections.emptyList();
        }
    }

    private LoanAccount toLoanAccount(LoanEntity loan) {
        LoanDetailsResponse loanDetailsResponse =
                apiClient.loanDetails(
                        new LoanDetailsRequest(
                                languageCode, loan.getRealEstateNumber(), loan.getLoanNumber()));

        return loan.toTinkLoan(loanDetailsResponse);
    }
}
