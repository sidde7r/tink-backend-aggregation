package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc;

import lombok.AllArgsConstructor;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
@AllArgsConstructor
public class SessionRequest {
    private String m1;
    private String publicA;
    private String token;
    private String authenticationMethod;
}
