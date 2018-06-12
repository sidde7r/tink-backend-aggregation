package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.AccountListEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ServicingFundsResponse {
    private AmountEntity total;
    private AccountListEntity accountList;
    private Object fundList;

    public AmountEntity getTotal() {
        return total;
    }

    public AccountListEntity getAccountList() {
        return accountList;
    }

    public Object getFundList() {
        return fundList;
    }
}

