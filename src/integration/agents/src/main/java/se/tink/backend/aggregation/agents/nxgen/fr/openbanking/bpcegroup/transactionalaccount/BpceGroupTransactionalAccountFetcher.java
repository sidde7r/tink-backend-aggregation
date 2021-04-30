package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient.BpceGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.base.BpceGroupBaseAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.converter.BpceGroupTransactionalAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.accounts.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.accounts.BalanceEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class BpceGroupTransactionalAccountFetcher
        extends BpceGroupBaseAccountFetcher<TransactionalAccount> {

    public BpceGroupTransactionalAccountFetcher(BpceGroupApiClient apiClient) {
        super(apiClient);
    }

    @Override
    protected boolean accountFilterPredicate(AccountEntity accountEntity) {
        return accountEntity.isTransactionalAccount();
    }

    @Override
    protected Optional<TransactionalAccount> map(
            AccountEntity accountEntity, List<BalanceEntity> balances) {
        return BpceGroupTransactionalAccountConverter.toTransactionalAccount(
                accountEntity, balances);
    }
}
