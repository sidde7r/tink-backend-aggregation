package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.utils.berlingroup.BerlingroupConstants.StatusValues;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {

    @Setter private String consentStatus;

    @Getter @Setter private String consentId;

    @JsonProperty("_links")
    @Getter
    private LinksEntity links;

    private String psuMessage;

    public boolean isNotAuthorized() {
        return StatusValues.RECEIVED.equalsIgnoreCase(consentStatus);
    }
}
