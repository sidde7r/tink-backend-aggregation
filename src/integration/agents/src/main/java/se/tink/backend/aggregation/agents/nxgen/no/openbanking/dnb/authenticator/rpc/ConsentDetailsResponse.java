package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.rpc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;

@JsonObject
@Getter
public class ConsentDetailsResponse {
    private static final String VALID = "valid";
    private String consentStatus;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate validUntil;

    public boolean isValid() {
        return VALID.equalsIgnoreCase(consentStatus);
    }
}
