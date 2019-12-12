package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PasswordAuthenticationLinksEntity {
    private Href selectAuthenticationMethod;
    private Href scaStatus;

    public Href getSelectAuthenticationMethod() {
        return selectAuthenticationMethod;
    }
}
