package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.entities.PersonEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.authentication.entities.SecurityKeyResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.rpc.OmaspBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse extends OmaspBaseResponse {
    private PersonEntity person;
    private SecurityKeyResponseEntity securityKey;
    private Boolean securityKeyRequired;
    private Boolean passwordRequired;

    public PersonEntity getPerson() {
        return person;
    }

    public SecurityKeyResponseEntity getSecurityKey() {
        return securityKey;
    }

    public Boolean getSecurityKeyRequired() {
        return securityKeyRequired;
    }

    public Boolean getPasswordRequired() {
        return passwordRequired;
    }
}
