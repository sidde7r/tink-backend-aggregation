package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.creditcard;

import java.util.Collection;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@RequiredArgsConstructor
public class LansforsakringarCreditCardFetcher implements AccountFetcher<CreditCardAccount> {

    private final LansforsakringarApiClient apiClient;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.getAccounts().getAccounts().stream()
                .filter(AccountEntity::isCreditCardAccount)
                .map(
                        accountEntity ->
                                accountEntity.toTinkCreditCardAccount(fetchBalances(accountEntity)))
                .collect(Collectors.toList());
    }

    private GetBalancesResponse fetchBalances(AccountEntity accountEntity) {
        return apiClient.getBalances(accountEntity.getResourceId());
    }
}
