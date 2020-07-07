package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class SibsTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final SibsBaseApiClient apiClient;

    public SibsTransactionalAccountFetcher(SibsBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountsResponse accountsResponse = apiClient.fetchAccounts();

        return accountsResponse.getAccountList().stream()
                .map(this::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    protected Optional<TransactionalAccount> toTinkAccount(AccountEntity accountEntity) {
        ExactCurrencyAmount balanceAmount =
                apiClient.getAccountBalances(accountEntity.getId()).getBalances().stream()
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                SibsConstants.ErrorMessages.NO_BALANCE))
                        .getInterimAvailable()
                        .getAmount()
                        .toTinkAmount();

        return accountEntity.toTinkAccount(balanceAmount);
    }
}
