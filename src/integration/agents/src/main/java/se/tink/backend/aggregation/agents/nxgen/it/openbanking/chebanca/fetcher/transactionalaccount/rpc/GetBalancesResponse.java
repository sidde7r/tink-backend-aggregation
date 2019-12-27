package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.BalancesDataEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.EmbededEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.ResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetBalancesResponse {
    @JsonProperty("_embeded")
    private EmbededEntity embedded;

    @JsonProperty("_links")
    private LinksEntity links;

    @JsonProperty("data")
    private BalancesDataEntity data;

    @JsonProperty("result")
    private ResultEntity result;

    @JsonIgnore
    public BalancesDataEntity getData() {
        return data;
    }
}
