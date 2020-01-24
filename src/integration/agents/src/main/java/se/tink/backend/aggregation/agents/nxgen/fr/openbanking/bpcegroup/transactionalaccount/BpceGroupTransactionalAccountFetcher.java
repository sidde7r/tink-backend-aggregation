package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.accounts.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class BpceGroupTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final BpceGroupApiClient bpceGroupApiClient;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final List<AccountEntity> accountEntities = getAccounts();

        if (accountEntities.isEmpty()) {
            return Collections.emptyList();
        }

        final List<String> accountIds =
                accountEntities.stream().map(AccountEntity::getIban).collect(Collectors.toList());

        bpceGroupApiClient.recordCustomerConsent(accountIds);

        return getAccounts().stream()
                .filter(AccountEntity::isTransactionalAccount)
                .map(AccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<AccountEntity> getAccounts() {
        return Optional.ofNullable(bpceGroupApiClient.fetchAccounts())
                .map(AccountsResponse::getAccounts)
                .orElseGet(Collections::emptyList);
    }
}
