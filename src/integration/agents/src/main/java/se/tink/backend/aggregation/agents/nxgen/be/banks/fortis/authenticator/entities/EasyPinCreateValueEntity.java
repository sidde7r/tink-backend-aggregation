package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class EasyPinCreateValueEntity {
    private String tokenId;
    private String userTokenId;
    private String registrationCode;
    private String enrollmentSessionId;
    private Object pin;
}
