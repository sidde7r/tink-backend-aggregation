package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankPredicates;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListLoansRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListLoansResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.LoanDetailsRequest;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DanskeBankLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final AggregationLogger LOG = new AggregationLogger(DanskeBankLoanFetcher.class);

    private final Credentials credentials;
    private final DanskeBankApiClient apiClient;
    private final DanskeBankConfiguration configuration;
    private final String languageCode;

    public DanskeBankLoanFetcher(Credentials credentials, DanskeBankApiClient apiClient,
            DanskeBankConfiguration configuration) {
        this.credentials = credentials;
        this.apiClient = apiClient;
        this.configuration = configuration;
        this.languageCode = configuration.getLanguageCode();
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        ListAccountsResponse listAccounts = apiClient.listAccounts(
                ListAccountsRequest.createFromLanguageCode(languageCode));

        // log any loan account of unknown product type
        listAccounts.getAccounts().stream()
                .filter(AccountEntity::isLoanAccount)
                .filter(DanskeBankPredicates.knownLoanAccountProducts(configuration.getLoanAccountTypes()).negate())
                .forEach(AccountEntity::logLoanAccount);

        listAndLogLoans();

        return listAccounts
                .getAccounts().stream()
                .filter(AccountEntity::isLoanAccount)
                .map(AccountEntity::toLoanAccount)
                .distinct()
                .collect(Collectors.toList());
    }

    private void listAndLogLoans() {
        try {
            String loansResponse = apiClient.listLoans(ListLoansRequest.createFromLanguageCode(languageCode));
            ListLoansResponse response = SerializationUtils.deserializeFromString(loansResponse, ListLoansResponse.class);
            if (response == null || response.getLoans() == null || response.getLoans().size() < 1) {
                return;
            }
            LOG.infoExtraLong(loansResponse, DanskeBankConstants.LogTags.LOAN_ACCOUNT);

            for (LoanEntity loan : response.getLoans()) {
                try {
                    String loanDetailsResponse = apiClient.loanDetails(
                            new LoanDetailsRequest(languageCode, loan.getRealEstateNumber(), loan.getLoanNumber()));
                    LOG.infoExtraLong(loanDetailsResponse, DanskeBankConstants.LogTags.LOAN_ACCOUNT);
                    String loanAgreementDetailsResponse = apiClient.loanAgreementDetails(
                            new LoanDetailsRequest(languageCode, loan.getRealEstateNumber(), loan.getLoanNumber()));
                    LOG.infoExtraLong(loanAgreementDetailsResponse, DanskeBankConstants.LogTags.LOAN_ACCOUNT);
                } catch (Exception e) {
                    LOG.info(String.format("%s Failed to fetch loan details [%s]",
                            DanskeBankConstants.LogTags.LOAN_ACCOUNT.toString(),
                            e.getMessage()));
                }
            }
        } catch (Exception e) {
            LOG.info(String.format("%s Failed to fetch loans [%s]",
                    DanskeBankConstants.LogTags.LOAN_ACCOUNT.toString(),
                    e.getMessage()));
        }
    }
}
