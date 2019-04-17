package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.entities.ErrorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.entities.GroupHeaderEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class NordeaBaseResponse {
    @JsonProperty("_type")
    private String type;

    @JsonProperty("group_header")
    private GroupHeaderEntity groupHeader;

    private ErrorEntity error;

    public void checkError() throws Exception {
        if (error != null) {
            error.parseAndThrow();
        }
    }
}
