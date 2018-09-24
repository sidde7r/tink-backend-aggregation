package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitSignRequest {
    @JsonProperty("Type")
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static InitSignRequest bundled() {
        InitSignRequest request = new InitSignRequest();
        request.setType(IcaBankenConstants.IdTags.BUNDLE_TAG);

        return request;
    }
}
