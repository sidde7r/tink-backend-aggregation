package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
public class ConsentDetailsResponse {

    private String consentStatus;

    @Getter
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate validUntil;

    public ConsentStatus getConsentStatus() {
        return ConsentStatus.valueOf(consentStatus.toUpperCase());
    }
}
