package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.tink.backend.aggregation.agents.utils.berlingroup.BerlingroupConstants.StatusValues;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;

@JsonObject
@NoArgsConstructor
@AllArgsConstructor
public class ConsentDetailsResponse {

    @JsonProperty @Setter private String consentStatus;

    @JsonProperty @Getter private String consentId;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @Getter
    @Setter
    private LocalDate validUntil;

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
