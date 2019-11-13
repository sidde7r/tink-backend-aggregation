package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.ConesntAuthorizationDataEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.ResourcesEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.ResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentAuthorizationResponse {
    private ConesntAuthorizationDataEntity data;
    private ResourcesEntity resources;
    private ResultEntity result;

    @JsonIgnore
    public ConesntAuthorizationDataEntity getData() {
        return data;
    }
}
