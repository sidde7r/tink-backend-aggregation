package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.rpc;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class LoginVMResponse {
    private Integer codeLength;
    private String recipient;
    private String token;
}
