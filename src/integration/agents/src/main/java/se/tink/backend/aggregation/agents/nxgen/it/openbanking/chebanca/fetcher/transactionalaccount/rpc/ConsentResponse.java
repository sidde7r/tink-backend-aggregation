package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.entities.ResultEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.ConsentDataEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.EmbededEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.ResourcesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {
    @JsonProperty("_embeded")
    private EmbededEntity embedded;

    @JsonProperty("data")
    private ConsentDataEntity data;

    @JsonProperty("resources")
    private ResourcesEntity resources;

    @JsonProperty("result")
    private ResultEntity result;

    public ResourcesEntity getResources() {
        return resources;
    }
}
