package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.transactions.TransactionsDataEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionalAccountTransactionsResponse extends BaseResponse {
    private String service;
    private TransactionsDataEntity data;

    public String getService() {
        return service;
    }

    public TransactionsDataEntity getData() {
        return data;
    }
}
