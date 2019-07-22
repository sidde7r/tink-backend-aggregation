package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Consent {

    private String consentId;
    private LocalDateTime consentCreated;

    public Consent(String consentId, LocalDateTime consentCreated) {
        this.consentId = consentId;
        this.consentCreated = consentCreated;
    }

    public LocalDateTime getConsentCreated() {
        return consentCreated;
    }

    public String getConsentId() {
        return consentId;
    }

    @JsonIgnore
    public boolean isConsentYoungerThan30Minutes() {
        return ChronoUnit.MINUTES.between(consentCreated, LocalDateTime.now()) <= 29;
    }
}
