package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse extends BaseResponse {
    private GlobalPosition globalPosition;
    private String nif;
    private String name;
    private String documentType;

    public String getNif() {
        return nif;
    }

    public String getName() {
        return name;
    }

    public String getDocumentType() {
        return documentType;
    }

    public GlobalPosition getGlobalPosition() {
        return globalPosition;
    }
}
