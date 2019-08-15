package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PasswordAuthenticationLinksEntity {
    private HrefEntity selectAuthenticationMethod;
    private HrefEntity scaStatus;

    public HrefEntity getSelectAuthenticationMethod() {
        return selectAuthenticationMethod;
    }
}
