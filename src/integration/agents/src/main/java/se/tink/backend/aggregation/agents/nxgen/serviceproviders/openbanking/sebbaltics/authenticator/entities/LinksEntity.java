package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private Href scaStatus;
    private Href selectAuthenticationMethod;

    public Href getScaStatus() {
        return scaStatus;
    }

    public Href getSelectAuthenticationMethod() {
        return selectAuthenticationMethod;
    }
}
