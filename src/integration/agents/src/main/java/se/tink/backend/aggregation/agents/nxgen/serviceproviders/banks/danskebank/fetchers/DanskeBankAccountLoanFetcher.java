package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Credentials credentials;
    private final DanskeBankApiClient apiClient;
    private final DanskeBankConfiguration configuration;
    private final String languageCode;

    public DanskeBankAccountLoanFetcher(
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
        ListAccountsResponse listAccounts =
                apiClient.listAccounts(ListAccountsRequest.createFromLanguageCode(languageCode));

        // log any loan account of unknown product type
        logUnknownLoanAccountTypes(listAccounts);

        return listAccounts.getAccounts().stream()
                .filter(AccountEntity::isLoanAccount)
                .map(account -> account.toLoanAccount(configuration))
                .distinct()
                .collect(Collectors.toList());
    }

    private void logUnknownLoanAccountTypes(ListAccountsResponse listAccounts) {
        listAccounts.getAccounts().stream()
                .filter(AccountEntity::isLoanAccount)
                .filter(
                        DanskeBankPredicates.knownLoanAccountProducts(
                                        configuration.getLoanAccountTypes())
                                .negate())
                .forEach(a -> logger.info("Unknown loan account type: " + a.getAccountType()));
    }
}
