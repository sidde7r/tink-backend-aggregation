package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.AbnAmroApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.AbnAmroConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.rpc.AccountHolderResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;

public class AbnAmroAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final AbnAmroApiClient apiClient;

    public AbnAmroAccountFetcher(final AbnAmroApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        return apiClient.fetchAccounts().stream()
                .map(AccountEntity::getAccountNumber)
                .map(accountId -> constructAccount(accountId))
                .collect(Collectors.toList());
    }

    private TransactionalAccount constructAccount(final String accountId) {

        AccountHolderResponse accountHolder = apiClient.fetchAccountHolder(accountId);
        AccountBalanceResponse balance = apiClient.fetchAccountBalance(accountId);

        return CheckingAccount.builder()
                .setUniqueIdentifier(accountId)
                .setAccountNumber(accountId)
                .setBalance(balance.toAmount())
                .setAlias(accountHolder.getAccountHolderName())
                .addAccountIdentifier(new IbanIdentifier(accountId))
                .addHolderName(accountHolder.getAccountHolderName())
                .putInTemporaryStorage(StorageKey.ACCOUNT_CONSENT_ID, accountId)
                .build();
    }
}
