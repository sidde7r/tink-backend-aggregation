package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.SamlinkConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TppMessageEntity {
    private String category;
    private String code;
    private String path;
    private String text;

    @JsonIgnore
    public boolean isConsentExpired() {
        return SamlinkConstants.ErrorCodes.CONSENT_EXPIRED.equalsIgnoreCase(code);
    }
}
