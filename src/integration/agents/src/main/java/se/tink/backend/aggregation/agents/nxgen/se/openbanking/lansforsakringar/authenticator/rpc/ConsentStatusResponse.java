package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import java.time.LocalDate;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentStatusResponse {

    private static final String VALID = "valid";

    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate validUntil;

    private String consentStatus;

    public boolean isConsentValid() {
        return VALID.equalsIgnoreCase(consentStatus)
                && (validUntil.isAfter(LocalDate.now()) || validUntil.isEqual(LocalDate.now()));
    }
}
