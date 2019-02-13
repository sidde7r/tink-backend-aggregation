package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankPredicates;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class DanskeBankAccountLoanFetcher implements AccountFetcher<LoanAccount> {

    private final Credentials credentials;
    private final DanskeBankApiClient apiClient;
    private final DanskeBankConfiguration configuration;
    private final String languageCode;

    public DanskeBankAccountLoanFetcher(Credentials credentials, DanskeBankApiClient apiClient,
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

        return listAccounts
                .getAccounts().stream()
                .filter(AccountEntity::isLoanAccount)
                .map(AccountEntity::toLoanAccount)
                .distinct()
                .collect(Collectors.toList());
    }
}
