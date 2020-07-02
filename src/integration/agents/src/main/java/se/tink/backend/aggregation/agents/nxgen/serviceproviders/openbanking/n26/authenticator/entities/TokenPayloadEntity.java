package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.entities;

import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@Setter
public class TokenPayloadEntity {

    private String expiresAtMs;
}
