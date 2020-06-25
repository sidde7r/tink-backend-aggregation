package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.entities.AccessTokenResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.StandardResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PollBankIdResponse extends StandardResponse {
    private AccessTokenResponseEntity accessTokenResponse;

    public AccessTokenResponseEntity getAccessTokenResponse() {
        return accessTokenResponse;
    }
}
