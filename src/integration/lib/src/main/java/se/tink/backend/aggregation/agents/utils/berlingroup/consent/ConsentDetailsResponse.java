package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import lombok.Getter;
import se.tink.backend.aggregation.agents.utils.berlingroup.BerlingroupConstants.StatusValues;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;

@JsonObject
public class ConsentDetailsResponse {

    @JsonProperty private String consentStatus;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @Getter
    private LocalDate validUntil;

    @JsonIgnore
    public boolean isValid() {
        return StatusValues.VALID.equalsIgnoreCase(consentStatus);
    }
}
