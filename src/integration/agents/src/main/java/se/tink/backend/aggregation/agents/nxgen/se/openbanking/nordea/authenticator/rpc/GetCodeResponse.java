
package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.entities.GroupHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.entities.ResponseCodeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetCodeResponse {

    @JsonProperty("group_header")
    private GroupHeaderEntity groupHeader;

    private ResponseCodeEntity response;

    public ResponseCodeEntity getResponse() {
        return response;
    }
}
