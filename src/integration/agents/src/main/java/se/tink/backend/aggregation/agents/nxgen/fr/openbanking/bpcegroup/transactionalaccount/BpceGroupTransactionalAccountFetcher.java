package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient.BpceGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.converter.BpceGroupTransactionalAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.accounts.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.accounts.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class BpceGroupTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final BpceGroupApiClient bpceGroupApiClient;
    private final BpceGroupTransactionalAccountConverter bpceGroupTransactionalAccountConverter;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final List<AccountEntity> accountEntities = getAccounts();

        if (accountEntities.isEmpty()) {
            return Collections.emptyList();
        }

        final List<String> accountIds =
                accountEntities.stream().map(AccountEntity::getIban).collect(Collectors.toList());

        bpceGroupApiClient.recordCustomerConsent(accountIds);

        return accountEntities.stream()
                .map(this::createTransactionalAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> createTransactionalAccount(AccountEntity accountEntity) {
        final List<BalanceEntity> balances = getBalances(accountEntity.getResourceId());

        return bpceGroupTransactionalAccountConverter.toTransactionalAccount(
                accountEntity, balances);
    }

    private List<AccountEntity> getAccounts() {
        return Optional.ofNullable(bpceGroupApiClient.fetchAccounts())
                .map(AccountsResponse::getAccounts)
                .map(
                        accounts ->
                                accounts.stream()
                                        .filter(AccountEntity::isTransactionalAccount)
                                        .collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
    }

    private List<BalanceEntity> getBalances(String resourceId) {
        return Optional.ofNullable(bpceGroupApiClient.fetchBalances(resourceId))
                .map(BalancesResponse::getBalances)
                .orElseGet(Collections::emptyList);
    }
}
