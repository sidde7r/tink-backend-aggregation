package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.creditcard;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.Xs2aDevelopersBaseAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenAccessor;

public class Xs2aDevelopersCreditCardAccountFetcher
        extends Xs2aDevelopersBaseAccountFetcher<CreditCardAccount> {

    public Xs2aDevelopersCreditCardAccountFetcher(
            Xs2aDevelopersApiClient apiClient, OAuth2TokenAccessor oAuth2TokenAccessor) {
        super(apiClient, oAuth2TokenAccessor);
    }

    @Override
    protected boolean accountFilterCondition(AccountEntity accountEntity) {
        return accountEntity.isCreditCardAccount();
    }

    @Override
    protected Optional<CreditCardAccount> transformAccount(AccountEntity accountEntity) {
        return Optional.of(accountEntity.toTinkCreditAccount());
    }
}
