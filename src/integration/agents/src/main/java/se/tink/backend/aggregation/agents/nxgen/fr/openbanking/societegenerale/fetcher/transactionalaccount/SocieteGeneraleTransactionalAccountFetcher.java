package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.base.SocieteGeneraleBaseAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities.AccountsItemEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SocieteGeneraleTransactionalAccountFetcher
        extends SocieteGeneraleBaseAccountFetcher<TransactionalAccount> {

    public SocieteGeneraleTransactionalAccountFetcher(SocieteGeneraleApiClient apiClient) {
        super(apiClient);
    }

    @Override
    protected boolean accountFilterCondition(AccountsItemEntity accountEntity) {
        return accountEntity.isCheckingAccount();
    }

    @Override
    protected Optional<TransactionalAccount> map(AccountsItemEntity accountEntity) {
        return accountEntity.toTinkTransactionalAccount();
    }
}
