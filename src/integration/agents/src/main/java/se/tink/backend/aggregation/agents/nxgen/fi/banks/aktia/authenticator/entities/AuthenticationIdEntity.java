package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationIdEntity {
    private String context;
    private String type;

    public AuthenticationIdEntity(String context) {
        this.context = context;
        this.type = AktiaConstants.Avain.AUTHENTICATION_ID_TYPE;
    }
}
