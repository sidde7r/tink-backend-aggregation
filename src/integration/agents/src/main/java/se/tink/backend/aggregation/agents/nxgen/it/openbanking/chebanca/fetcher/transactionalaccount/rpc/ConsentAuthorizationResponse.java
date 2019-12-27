package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.ConesntAuthorizationDataEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.ResourcesEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.ResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentAuthorizationResponse {
    @JsonProperty("data")
    private ConesntAuthorizationDataEntity data;

    @JsonProperty("resources")
    private ResourcesEntity resources;

    @JsonProperty("result")
    private ResultEntity result;

    public ConesntAuthorizationDataEntity getData() {
        return data;
    }
}
