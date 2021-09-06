package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo.fetcher;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.Xs2aDevelopersTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenAccessor;

public class UniversoAccountFetcher extends Xs2aDevelopersTransactionalAccountFetcher {
    private static final String CREDIT_NAME = "Credit";

    public UniversoAccountFetcher(
            Xs2aDevelopersApiClient apiClient, OAuth2TokenAccessor oAuth2TokenAccessor) {
        super(apiClient, oAuth2TokenAccessor);
    }

    @Override
    protected boolean accountFilterCondition(AccountEntity accountEntity) {
        return super.accountFilterCondition(accountEntity)
                && CREDIT_NAME.equals(accountEntity.getName());
    }
}
