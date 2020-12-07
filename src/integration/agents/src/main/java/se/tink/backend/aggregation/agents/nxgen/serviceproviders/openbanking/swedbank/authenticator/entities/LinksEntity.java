package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private Href scaStatus;

    private Href status;

    private Href startAuthorisationWithAuthenticationMethodSelection;

    private Href selectAuthenticationMethod;

    public Href getScaStatus() {
        return scaStatus;
    }

    public Href getHrefEntity() {
        return startAuthorisationWithAuthenticationMethodSelection;
    }

    public Href getStatus() {
        return status;
    }

    public Href getSelectAuthenticationMethod() {
        return selectAuthenticationMethod;
    }
}
