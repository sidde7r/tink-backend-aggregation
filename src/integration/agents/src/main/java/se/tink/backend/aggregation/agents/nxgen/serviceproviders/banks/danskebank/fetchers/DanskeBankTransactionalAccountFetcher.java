package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankPredicates;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper.AccountEntityMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

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

        List<AccountEntity> accountEntities = listAccounts.getAccounts();

        Map<String, AccountDetailsResponse> accountDetails = new HashMap<>();
        for (AccountEntity accountEntity : accountEntities) {
            accountDetails.put(accountEntity.getAccountNoExt(), fetchAccountDetails(accountEntity));
        }

        logDuplicatedAccountNoExt(listAccounts.getAccounts());

        return ImmutableList.<TransactionalAccount>builder()
                .addAll(logAndGetTransactionalAccountsOfUnknownType(listAccounts))
                .addAll(
                        accountEntityMapper.toTinkCheckingAccounts(
                                configuration.getCheckingAccountTypes(),
                                listAccounts.getAccounts(),
                                accountDetails))
                .addAll(
                        accountEntityMapper.toTinkSavingsAccounts(
                                configuration, listAccounts.getAccounts(), accountDetails))
                .build();
    }

    private AccountDetailsResponse fetchAccountDetails(AccountEntity accountEntity) {
        // Using "EN" as languageCode to be consistent with fetchLoanAccountDetails().
        try {
            return apiClient.fetchAccountDetails(
                    new AccountDetailsRequest(accountEntity.getAccountNoInt(), "EN"));
        } catch (HttpResponseException e) {
            // Sometimes we receive 500 response that has a body of AccountDetailsResponse
            if (e.getResponse().getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR
                    && e.getResponse().hasBody()) {
                try {
                    return e.getResponse().getBody(AccountDetailsResponse.class);
                } catch (RuntimeException re) {
                    log.info("Failed to map exception body into AccountDetailsResponse. ", e);
                }
            }
            log.info("Failed to fetch account details. ", e);
        }
        return new AccountDetailsResponse();
    }

    private List<TransactionalAccount> logAndGetTransactionalAccountsOfUnknownType(
            ListAccountsResponse listAccounts) {
        List<AccountEntity> accountEntities = listAccounts.getAccounts();

        Map<String, AccountDetailsResponse> accountDetails = new HashMap<>();
        for (AccountEntity accountEntity : accountEntities) {
            accountDetails.put(accountEntity.getAccountNoExt(), fetchAccountDetails(accountEntity));
        }

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
                .map(
                        accountEntity ->
                                accountEntityMapper.toUnknownAccount(
                                        accountEntity,
                                        accountDetails.get(accountEntity.getAccountNoExt())))
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
