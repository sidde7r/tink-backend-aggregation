package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.enums.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.TppMessageEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {
    @JsonProperty private AccessEntity access;
    @JsonProperty private boolean recurringIndicator;
    @JsonProperty private String validUntil;
    @JsonProperty private int frequencyPerDay;
    @JsonProperty private String lastActionDate;
    @JsonProperty private String consentStatus;
    @JsonProperty private String psuMessage;
    @JsonProperty private List<TppMessageEntity> tppMessages;

    @JsonIgnore
    public ConsentStatus getConsentStatus() {
        return ConsentStatus.fromString(consentStatus);
    }

    @JsonIgnore
    public boolean isConsentValid() {
        return getConsentStatus() == ConsentStatus.VALID;
    }
}
