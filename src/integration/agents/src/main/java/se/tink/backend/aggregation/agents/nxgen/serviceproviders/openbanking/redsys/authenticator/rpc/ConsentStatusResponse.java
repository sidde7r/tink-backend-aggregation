package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.TppMessageEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentStatusResponse {
    @JsonProperty private String psuMessage;
    @JsonProperty private List<TppMessageEntity> tppMessages;
    @JsonProperty private String consentStatus;

    public String getConsentStatus() {
        return consentStatus;
    }
}
