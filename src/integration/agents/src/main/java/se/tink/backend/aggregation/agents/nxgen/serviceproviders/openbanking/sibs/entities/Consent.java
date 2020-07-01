package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Consent {

    private String consentId;
    private String consentCreated;

    public Consent() {}

    public Consent(String consentId, String consentCreated) {
        this.consentId = consentId;
        this.consentCreated = consentCreated;
    }

    public String getConsentCreated() {
        return consentCreated;
    }

    public String getConsentId() {
        return consentId;
    }

    @JsonIgnore
    public boolean isConsentOlderThan30Minutes() {
        LocalDateTime created = LocalDateTime.parse(consentCreated);
        return ChronoUnit.MINUTES.between(created, LocalDateTime.now()) > 29;
    }
}
