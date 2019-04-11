package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.rpc;

import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.entity.AccountsResultsEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.rpc.GenericResponse;

public class AccountsResponse extends GenericResponse<AccountsResultsEntity> {

    public Stream<AccountEntity> stream() {
        return results.stream();
    }
}
