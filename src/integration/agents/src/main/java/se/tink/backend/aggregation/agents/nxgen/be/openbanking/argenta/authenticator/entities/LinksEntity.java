package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@SuppressWarnings("unused")
public class LinksEntity {

    private Href self;
    private Href scaOAuth;
    private Href status;
    private Href selectAuthenticationMethod;

    public Href getSelectAuthenticationMethod() {
        return selectAuthenticationMethod;
    }
}
