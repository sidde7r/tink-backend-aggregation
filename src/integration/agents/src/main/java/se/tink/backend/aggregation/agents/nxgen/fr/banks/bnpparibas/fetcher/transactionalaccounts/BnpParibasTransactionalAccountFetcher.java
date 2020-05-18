package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    private TransactionalAccount convertToTinkCheckingAccount(AccountEntity accountEntity) {
        List<TransactionAccountEntity> accountIbanDetails = apiClient.getAccountIbanDetails();
        Optional<TransactionAccountEntity> maybeIbanDetails =
                accountIbanDetails.stream()
                        .filter(
                                transactionAccountEntity ->
                                        transactionAccountEntity
                                                .getIbanKey()
                                                .equals(accountEntity.getIbanKey()))
                        .findFirst();
        if (!maybeIbanDetails.isPresent()) {
            return null;
        }
        String iban = maybeIbanDetails.get().getIban();
        return accountEntity.toTinkCheckingAccount(iban);
    }

    private TransactionalAccount convertToTinkSavingsAccount(AccountEntity accountEntity) {
        List<TransactionAccountEntity> accountIbanDetails = apiClient.getAccountIbanDetails();
        Optional<TransactionAccountEntity> maybeIbanDetails =
                accountIbanDetails.stream()
                        .filter(
                                transactionAccountEntity ->
                                        transactionAccountEntity
                                                .getIbanKey()
                                                .equals(accountEntity.getIbanKey()))
                        .findFirst();
        if (!maybeIbanDetails.isPresent()) {
            return null;
        }
        String iban = maybeIbanDetails.get().getIban();
        return accountEntity.toTinkSavingsAccount(iban);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        InfoUdcEntity infoUdc = apiClient.getAccounts();
        List<TransactionalAccount> accounts = new ArrayList<>();

        List<TransactionalAccount> checking =
                infoUdc.getCheckingsAccounts().stream()
                        .map(this::convertToTinkCheckingAccount)
                        .collect(Collectors.toList());
        List<TransactionalAccount> savings =
                infoUdc.getSavingsAccounts().stream()
                        .map(this::convertToTinkSavingsAccount)
                        .collect(Collectors.toList());

        accounts.addAll(checking);
        accounts.addAll(savings);

        return accounts;
    }
}
