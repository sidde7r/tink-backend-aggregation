package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.BnpParibasApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts.TransactionAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.transactions.InfoUdcEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class BnpParibasTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final BnpParibasApiClient apiClient;

    public BnpParibasTransactionalAccountFetcher(BnpParibasApiClient apiClient) {
        this.apiClient = apiClient;
    }

    private Optional<TransactionalAccount> convertToTinkCheckingAccount(
            AccountEntity accountEntity, Map<String, String> ibansByKey) {

        final String iban = ibansByKey.get(accountEntity.getIbanKey());

        return accountEntity.toTinkCheckingAccount(iban);
    }

    private Optional<TransactionalAccount> convertToTinkSavingsAccount(
            AccountEntity accountEntity, Map<String, String> ibansByKey) {

        final String iban = ibansByKey.get(accountEntity.getIbanKey());

        return accountEntity.toTinkSavingsAccount(iban);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        InfoUdcEntity infoUdc = apiClient.getAccounts();
        List<TransactionalAccount> accounts = new ArrayList<>();

        final Map<String, String> ibansByKey =
                apiClient.getAccountIbanDetails().stream()
                        .collect(
                                Collectors.toMap(
                                        TransactionAccountEntity::getIbanKey,
                                        TransactionAccountEntity::getIban));

        List<TransactionalAccount> checking =
                infoUdc.getCheckingsAccounts().stream()
                        .map(account -> convertToTinkCheckingAccount(account, ibansByKey))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
        List<TransactionalAccount> savings =
                infoUdc.getSavingsAccounts().stream()
                        .map(account -> convertToTinkSavingsAccount(account, ibansByKey))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

        accounts.addAll(checking);
        accounts.addAll(savings);

        return accounts;
    }
}
