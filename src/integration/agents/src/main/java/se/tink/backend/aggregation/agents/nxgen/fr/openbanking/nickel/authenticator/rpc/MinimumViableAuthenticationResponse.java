package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.rpc;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class MinimumViableAuthenticationResponse {
    private String accessToken;
    private String culture;
    private Integer customerId;
    private String customerType;
    private String registrationCountry;
}
