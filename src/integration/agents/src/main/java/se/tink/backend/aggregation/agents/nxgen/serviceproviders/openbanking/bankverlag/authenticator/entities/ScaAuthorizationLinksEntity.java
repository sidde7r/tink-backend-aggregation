package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.entities;

import lombok.Getter;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ScaAuthorizationLinksEntity {

    private Href scaStatus;
    private Href authoriseTransaction;
    private Href selectAuthenticationMethod;
}
