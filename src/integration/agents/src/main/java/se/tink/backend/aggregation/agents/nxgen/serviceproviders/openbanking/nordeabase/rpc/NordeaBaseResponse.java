package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.entities.GroupHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class NordeaBaseResponse {
    @JsonProperty("_type")
    private String type;

    @JsonProperty("group_header")
    private GroupHeaderEntity groupHeader;

    @JsonProperty("_links")
    private List<LinkEntity> links;

    public List<LinkEntity> getLinks() {
        return links;
    }
}
