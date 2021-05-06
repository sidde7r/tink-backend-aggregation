package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.asserts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts.TokenEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConfirmChallengeResponse {
    private TokenEntity data;

    @JsonIgnore
    public String getToken() {
        return data.getToken();
    }
}
