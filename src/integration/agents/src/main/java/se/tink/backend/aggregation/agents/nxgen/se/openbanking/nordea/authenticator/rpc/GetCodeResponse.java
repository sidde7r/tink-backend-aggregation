package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.entities.ResponseCodeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc.NordeaBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetCodeResponse extends NordeaBaseResponse {

    private ResponseCodeEntity response;

    public ResponseCodeEntity getResponse() {
        return response;
    }
}
