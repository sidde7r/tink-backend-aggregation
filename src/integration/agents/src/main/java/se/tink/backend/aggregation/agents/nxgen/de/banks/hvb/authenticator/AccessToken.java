package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AccessToken {

    String tokenValue;
    String tokenType;
    Long expiresIn;
    String scope;
}
