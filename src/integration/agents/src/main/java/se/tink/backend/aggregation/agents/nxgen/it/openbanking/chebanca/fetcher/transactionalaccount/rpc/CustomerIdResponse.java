package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.DataEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.ResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustomerIdResponse {
    @JsonProperty("data")
    private DataEntity data;

    @JsonProperty("result")
    private ResultEntity result;

    public DataEntity getData() {
        return data;
    }

    public ResultEntity getResult() {
        return result;
    }
}
