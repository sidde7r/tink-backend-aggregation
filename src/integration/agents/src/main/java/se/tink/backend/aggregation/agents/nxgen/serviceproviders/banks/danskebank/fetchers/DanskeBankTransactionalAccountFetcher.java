package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankPredicates;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper.AccountEntityMapper;
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
    private final AccountEntityMapper accountEntityMapper;

    public DanskeBankTransactionalAccountFetcher(
            DanskeBankApiClient apiClient,
            DanskeBankConfiguration configuration,
            AccountEntityMapper accountEntityMapper) {
        this.apiClient = apiClient;
        this.configuration = configuration;
        this.accountEntityMapper = accountEntityMapper;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        ListAccountsResponse listAccounts =
                apiClient.listAccounts(
                        ListAccountsRequest.createFromLanguageCode(
                                configuration.getLanguageCode()));

        logDuplicatedAccountNoExt(listAccounts.getAccounts());

        return ImmutableList.<TransactionalAccount>builder()
                .addAll(logAndGetTransactionalAccountsOfUnknownType(listAccounts))
                .addAll(
                        accountEntityMapper.toTinkCheckingAccounts(
                                configuration.getCheckingAccountTypes(),
                                listAccounts.getAccounts()))
                .addAll(
                        accountEntityMapper.toTinkSavingsAccounts(
                                configuration, listAccounts.getAccounts()))
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
                .map(accountEntityMapper::toUnknownAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    List<AccountEntity> logDuplicatedAccountNoExt(List<AccountEntity> accounts) {
        List<String> accountsNoExt =
                accounts.stream().map(AccountEntity::getAccountNoExt).collect(Collectors.toList());
        List<AccountEntity> accountIds =
                accounts.stream()
                        .filter(
                                account ->
                                        isAccountNumberFrequencyMoreThanOne(
                                                accountsNoExt, account.getAccountNoExt()))
                        .distinct()
                        .collect(Collectors.toList());
        logDuplicates(accountIds);
        return accountIds;
    }

    private boolean isAccountNumberFrequencyMoreThanOne(
            List<String> accountsNoExt, String accountNumber) {
        return Collections.frequency(accountsNoExt, accountNumber) > 1;
    }

    private void logDuplicates(List<AccountEntity> duplicates) {
        if (!duplicates.isEmpty()) {
            log.error(
                    "There were [{}] duplicated accounts in bank's response: {} !",
                    duplicates.size(),
                    duplicates.stream()
                            .map(
                                    account ->
                                            String.format(
                                                    "[accountNoExt: %s, accountNoInt: %s]",
                                                    account.getAccountNoExt(),
                                                    account.getAccountNoInt()))
                            .collect(Collectors.joining(", ")));
        }
    }
}
