package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.entity.AccountDetailsResultEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.rpc.GenericResponse;

public class AccountDetailsResponse extends GenericResponse<AccountDetailsResultEntity> {
    public AccountDetailsResultEntity getResult() {
        return results;
    }
}
