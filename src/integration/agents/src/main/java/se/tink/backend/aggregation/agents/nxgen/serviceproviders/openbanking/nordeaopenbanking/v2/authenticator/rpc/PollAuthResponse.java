package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.authenticator.entities.PollAuthResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.rpc.NordeaResponseBase;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PollAuthResponse extends NordeaResponseBase {
    private PollAuthResponseEntity response;

    public String getCode() {
        return response.getCode();
    }

    private String getState() {
        return response.getState();
    }

    public String getTokenPath() {
        return response.findLinkByName(NordeaBaseConstants.Link.TOKEN_LINK)
                .orElseThrow(() -> new IllegalStateException("No poll auth link found"))
                .getHref();
    }

    @JsonIgnore
    public boolean isCompleted() {
        return NordeaBaseConstants.Authorization.STATE_COMPLETED.equalsIgnoreCase(getState());
    }
}
