package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.creditcard;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class LansforsakringarCreditCardFetcher implements AccountFetcher<CreditCardAccount> {

    private final LansforsakringarApiClient apiClient;

    public LansforsakringarCreditCardFetcher(LansforsakringarApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.getAccounts().getAccounts().stream()
                .filter(AccountEntity::isCreditCardAccount)
                .map(AccountEntity::toTinkCreditCardAccount)
                .collect(Collectors.toList());
    }
}
