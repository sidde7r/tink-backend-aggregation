package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transactionalaccount.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transactionalaccount.entity.AccountDetailsResultEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.rpc.GenericResponse;

public class AccountDetailsResponse extends GenericResponse<AccountDetailsResultEntity> {
    public AccountDetailsResultEntity getResult() {
        return results;
    }
}
