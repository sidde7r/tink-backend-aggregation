package se.tink.backend.aggregation.agents.utils.berlingroup.common;

import lombok.Getter;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LinksEntity {

    private Href startAuthorisationWithPsuAuthentication;
    private Href startAuthorisationWithEncryptedPsuAuthentication;
    private Href scaOAuth;
    private Href scaRedirect;
    private Href scaStatus;
    private Href self;
    private Href status;
    private Href authoriseTransaction;
    private Href selectAuthenticationMethod;
}
