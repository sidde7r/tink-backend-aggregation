package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.ResultEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.TransactionsDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetTransactionsResponse {
    @JsonProperty("_links")
    private LinksEntity links;

    @JsonProperty("data")
    private TransactionsDataEntity data;

    @JsonProperty("result")
    private ResultEntity result;

    public LinksEntity getLinks() {
        return links;
    }

    public TransactionsDataEntity getData() {
        return data;
    }

    public ResultEntity getResult() {
        return result;
    }
}
