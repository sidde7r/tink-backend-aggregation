package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.StatusValues;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;

@JsonObject
public class ConsentDetailsResponse {

    @JsonProperty private String consentStatus;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate validUntil;

    public LocalDate getValidUntil() {
        return validUntil;
    }

    @JsonIgnore
    public boolean isValid() {
        return StatusValues.VALID.equalsIgnoreCase(consentStatus);
    }
}
