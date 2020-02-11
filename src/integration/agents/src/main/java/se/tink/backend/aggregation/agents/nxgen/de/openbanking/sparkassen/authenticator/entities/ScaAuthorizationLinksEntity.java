package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaAuthorizationLinksEntity {

    private Href scaStatus;
    private Href authoriseTransaction;
    private Href selectAuthenticationMethod;
}
