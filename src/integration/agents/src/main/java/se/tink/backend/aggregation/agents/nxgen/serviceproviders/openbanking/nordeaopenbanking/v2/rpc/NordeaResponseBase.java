package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.entities.GroupHeaderEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NordeaResponseBase {
    private GroupHeaderEntity groupHeader;

    public GroupHeaderEntity getGroupHeader() {
        return groupHeader;
    }
}
