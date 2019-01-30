package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.entities.GroupHeaderEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NordeaResponseBase {
    @JsonProperty("group_header")
    private GroupHeaderEntity groupHeader;

    public GroupHeaderEntity getGroupHeader() {
        return groupHeader;
    }
}
