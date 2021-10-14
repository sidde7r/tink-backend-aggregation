package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.tink.backend.aggregation.agents.utils.berlingroup.BerlingroupConstants.StatusValues;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@NoArgsConstructor
@AllArgsConstructor
public class ConsentDetailsResponse {

    @Getter private AccessEntity access;
    @Setter private String consentStatus;
    @Getter private String consentId;
    @Setter private String validUntil;

    public LocalDate getValidUntil() {
        return LocalDate.parse(validUntil, DateTimeFormatter.ISO_DATE);
    }

    public LocalDateTime getValidUntil(DateTimeFormatter formatter) {
        return LocalDateTime.parse(validUntil, formatter);
    }

    public boolean isValid() {
        return StatusValues.VALID.equalsIgnoreCase(consentStatus);
    }

    public boolean isExpired() {
        return StatusValues.EXPIRED.equalsIgnoreCase(consentStatus);
    }

    public boolean isRevokedByPsu() {
        return StatusValues.REVOKED_BY_PSU.equalsIgnoreCase(consentStatus);
    }
}
