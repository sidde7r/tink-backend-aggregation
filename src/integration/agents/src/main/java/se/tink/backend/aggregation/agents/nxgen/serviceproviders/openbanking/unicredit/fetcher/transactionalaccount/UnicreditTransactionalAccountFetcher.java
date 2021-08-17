package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class UnicreditTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final UnicreditBaseApiClient apiClient;
    private final UnicreditTransactionalAccountMapper mapper;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().getAccounts().stream()
                .map(this::fetchAdditionalAndTransform)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public Optional<TransactionalAccount> fetchAdditionalAndTransform(AccountEntity accountEntity) {
        String resourceId = accountEntity.getResourceId();
        return mapper.toTinkAccount(
                apiClient.fetchAccountDetails(resourceId).getAccount(),
                apiClient.fetchAccountBalance(resourceId).getBalances());
    }
}
