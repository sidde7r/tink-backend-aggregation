package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.authenticator.entities.InitAuthResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.rpc.NordeaResponseBase;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitAuthResponse extends NordeaResponseBase {
    private InitAuthResponseEntity response;

    public String getOrderRef() {
        return response.getOrderRef();
    }

    @JsonIgnore
    public String tppTokenAsHeaderValue() {
        return NordeaBaseConstants.Authorization.tokenToAuthorizationValue(response.getTppToken());
    }

    public String getCollectPath() {
        return response.findLinkByName(NordeaBaseConstants.Link.POLL_AUTH_LINK)
                .orElseThrow(() -> new IllegalStateException("No poll auth link found"))
                .getHref();
    }
}
