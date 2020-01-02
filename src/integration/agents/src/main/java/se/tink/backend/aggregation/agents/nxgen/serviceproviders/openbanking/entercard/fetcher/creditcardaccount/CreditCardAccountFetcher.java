package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.creditcardaccount;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class CreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {
    private final EnterCardApiClient apiClient;
    private final String ssn;
    private final String brandId;

    public CreditCardAccountFetcher(EnterCardApiClient apiClient, String ssn, String brandId) {
        this.apiClient = apiClient;
        this.ssn = ssn;
        this.brandId = brandId;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.fetchCreditCardAccounts(ssn).getAccount().stream()
                .filter(AccountEntity::isCreditCardAccount)
                .filter(accountEntity -> accountEntity.isBrandId(brandId))
                .map(AccountEntity::toCreditCardAccount)
                .collect(Collectors.toList());
    }
}
