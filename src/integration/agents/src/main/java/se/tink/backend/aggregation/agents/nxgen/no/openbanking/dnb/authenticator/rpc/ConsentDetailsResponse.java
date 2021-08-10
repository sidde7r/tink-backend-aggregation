package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.rpc;

import java.time.LocalDate;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ConsentDetailsResponse {
    private static final String VALID = "valid";
    private String consentStatus;

    private LocalDate validUntil;

    public boolean isValid() {
        return VALID.equalsIgnoreCase(consentStatus);
    }
}
