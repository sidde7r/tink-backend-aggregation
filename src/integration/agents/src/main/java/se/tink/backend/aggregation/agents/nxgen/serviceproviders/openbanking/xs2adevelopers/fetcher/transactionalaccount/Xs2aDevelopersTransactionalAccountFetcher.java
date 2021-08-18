package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount;

import java.util.Arrays;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.Xs2aDevelopersBaseAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenAccessor;

@Slf4j
public class Xs2aDevelopersTransactionalAccountFetcher
        extends Xs2aDevelopersBaseAccountFetcher<TransactionalAccount> {

    public Xs2aDevelopersTransactionalAccountFetcher(
            Xs2aDevelopersApiClient apiClient, OAuth2TokenAccessor oAuth2TokenAccessor) {
        super(apiClient, oAuth2TokenAccessor);
    }

    @Override
    protected Optional<TransactionalAccount> transformAccount(AccountEntity accountEntity) {
        return accountEntity.toTinkAccount();
    }

    @Override
    protected boolean accountFilterCondition(AccountEntity accountEntity) {
        return Xs2aDevelopersConstants.ACCOUNT_TYPE_MAPPER.isOneOf(
                accountEntity.getAccountType(),
                Arrays.asList(AccountTypes.CHECKING, AccountTypes.SAVINGS));
    }
}
