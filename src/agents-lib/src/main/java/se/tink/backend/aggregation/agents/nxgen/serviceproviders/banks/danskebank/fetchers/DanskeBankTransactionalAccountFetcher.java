package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankPredicates;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.agents.rpc.Credentials;

public class DanskeBankTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final Credentials credentials;
    private final DanskeBankApiClient apiClient;
    private final DanskeBankConfiguration configuration;
    private final String languageCode;

    public DanskeBankTransactionalAccountFetcher(Credentials credentials, DanskeBankApiClient apiClient,
            DanskeBankConfiguration configuration) {
        this.credentials = credentials;
        this.apiClient = apiClient;
        this.configuration = configuration;
        this.languageCode = configuration.getLanguageCode();
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        ListAccountsResponse listAccounts = apiClient.listAccounts(
                ListAccountsRequest.createFromLanguageCode(languageCode));

        return ImmutableList.<TransactionalAccount>builder()
                .addAll(logAndGetTransactionalAccountsOfUnknownType(listAccounts))
                .addAll(listAccounts.toTinkCheckingAccounts(configuration.getCheckingAccountTypes()))
                .addAll(listAccounts.toTinkSavingsAccounts(configuration.getSavingsAccountTypes())).build();
    }

    private List<TransactionalAccount> logAndGetTransactionalAccountsOfUnknownType(ListAccountsResponse listAccounts) {
        return  listAccounts.getAccounts().stream()
                .filter(DanskeBankPredicates.CREDIT_CARDS.negate())
                .filter(accountEntity -> !accountEntity.isLoanAccount())
                .filter(DanskeBankPredicates.knownCheckingAccountProducts(configuration.getCheckingAccountTypes())
                        .negate())
                .filter(DanskeBankPredicates.knownSavingsAccountProducts(configuration.getSavingsAccountTypes())
                        .negate())
                .map(accountEntity -> {
                    accountEntity.logTransactionalAccount();
                    return accountEntity.toCheckingAccount();
                }).collect(Collectors.toList());
    }
}
