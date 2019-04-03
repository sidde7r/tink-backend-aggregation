
package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.rpc;


import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.entity.account.AccountDataEntity;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.entity.common.ResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BBVAAccountsResponse {
    private ResultEntity result;
    private AccountDataEntity data;

    public AccountDataEntity getData() {
        return data;
    }
}
