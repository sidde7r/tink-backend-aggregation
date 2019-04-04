package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.entity.account.DetailedDataEntity;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.entity.common.ResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class BBVADetailedAccountResponse {
    private ResultEntity result;
    private DetailedDataEntity data;

    public TransactionalAccount toTinkAccount() {
        return data.getAccount().ToTinkAccount();
    }
}
