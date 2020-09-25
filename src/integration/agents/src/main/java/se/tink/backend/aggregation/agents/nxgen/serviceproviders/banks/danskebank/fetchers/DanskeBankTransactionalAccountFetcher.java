package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankPredicates;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class DanskeBankTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private static final Logger log =
            LoggerFactory.getLogger(DanskeBankTransactionalAccountFetcher.class);

    private final DanskeBankApiClient apiClient;
    private final DanskeBankConfiguration configuration;
    private final String languageCode;

    public DanskeBankTransactionalAccountFetcher(
            DanskeBankApiClient apiClient, DanskeBankConfiguration configuration) {
        this.apiClient = apiClient;
        this.configuration = configuration;
        this.languageCode = configuration.getLanguageCode();
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        ListAccountsResponse listAccounts =
                apiClient.listAccounts(ListAccountsRequest.createFromLanguageCode(languageCode));

        return ImmutableList.<TransactionalAccount>builder()
                .addAll(logAndGetTransactionalAccountsOfUnknownType(listAccounts))
                .addAll(
                        listAccounts.toTinkCheckingAccounts(
                                configuration.getCheckingAccountTypes()))
                .addAll(listAccounts.toTinkSavingsAccounts(configuration.getSavingsAccountTypes()))
                .build();
    }

    private List<TransactionalAccount> logAndGetTransactionalAccountsOfUnknownType(
            ListAccountsResponse listAccounts) {
        return listAccounts.getAccounts().stream()
                .filter(DanskeBankPredicates.CREDIT_CARDS.negate())
                .filter(accountEntity -> !accountEntity.isLoanAccount())
                .filter(
                        DanskeBankPredicates.knownCheckingAccountProducts(
                                        configuration.getCheckingAccountTypes())
                                .negate())
                .filter(
                        DanskeBankPredicates.knownSavingsAccountProducts(
                                        configuration.getSavingsAccountTypes())
                                .negate())
                .peek(
                        accountEntity ->
                                log.info(
                                        "Account: apiIdentifier = {}, accountProduct = {}",
                                        accountEntity.getAccountNoInt(),
                                        accountEntity.getAccountProduct()))
                .map(AccountEntity::toCheckingAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
