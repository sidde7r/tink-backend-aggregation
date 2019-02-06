package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.rpc;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.entity.DataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountsResponse {
    private DataEntity data;

    public Collection<TransactionalAccount> toTransactionalAccounts(String holdername) {
        return data.toTransactionalAccounts(holdername);
    }
}
