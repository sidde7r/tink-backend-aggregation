package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.base.LaBanquePostaleBaseAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.converter.LaBanquePostaleTransactionalAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class LaBanquePostaleTransactionalAccountFetcher
        extends LaBanquePostaleBaseAccountFetcher<TransactionalAccount> {

    public LaBanquePostaleTransactionalAccountFetcher(
            LaBanquePostaleApiClient laBanquePostaleApiClient,
            LaBanquePostaleTransactionalAccountConverter accountConverter) {
        super(laBanquePostaleApiClient);
        this.accountConverter = accountConverter;
    }

    private final LaBanquePostaleTransactionalAccountConverter accountConverter;

    @Override
    protected boolean accountTypeFilterCondition(AccountEntity accountEntity) {
        return accountEntity.isTransactionalAccount();
    }

    @Override
    protected Optional<TransactionalAccount> map(AccountEntity accountEntity) {
        return accountConverter.toTransactionalAccount(accountEntity);
    }
}
