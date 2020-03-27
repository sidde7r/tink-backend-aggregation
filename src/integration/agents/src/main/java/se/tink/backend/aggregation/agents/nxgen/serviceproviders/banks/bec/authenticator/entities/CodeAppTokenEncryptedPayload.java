package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CodeAppTokenEncryptedPayload {
    @JsonProperty("codeapp")
    private CodeAppTokenEntity codeappTokenDetails;

    public CodeAppTokenEntity getCodeappTokenDetails() {
        return codeappTokenDetails;
    }
}
