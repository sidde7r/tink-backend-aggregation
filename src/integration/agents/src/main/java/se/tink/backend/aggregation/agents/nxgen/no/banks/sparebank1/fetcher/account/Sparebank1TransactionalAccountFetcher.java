package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.account;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.refresh.CheckingAccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.account.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.account.rpc.AccountApiIdentifiersResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class Sparebank1TransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final Sparebank1ApiClient apiClient;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountApiIdentifiersResponse apiIdentifiers = apiClient.fetchAccountApiIdentifiers();

        return apiClient.fetchAccounts().getAccounts().stream()
                .map(acc -> mapAccountApiIdentifier(acc, apiIdentifiers))
                .map(acc -> acc.toTransactionalAccount(apiClient.fetchAccountDetails(acc.getKey())))
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private AccountEntity mapAccountApiIdentifier(
            AccountEntity accountEntity, AccountApiIdentifiersResponse apiIdentifiers) {
        String apiIdentifier =
                apiIdentifiers.getAccounts().stream()
                        .filter(
                                account ->
                                        account.getAccountNumber()
                                                .equals(accountEntity.getAccountNumber()))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new CheckingAccountRefreshException(
                                                "Can't map account to apiIdentifier list accounts"))
                        .getKey();

        if (apiIdentifier == null) {
            throw new CheckingAccountRefreshException(
                    "No apiIdentifier for account: " + accountEntity.getAccountNumber());
        }
        accountEntity.setKey(apiIdentifier);
        return accountEntity;
    }
}
