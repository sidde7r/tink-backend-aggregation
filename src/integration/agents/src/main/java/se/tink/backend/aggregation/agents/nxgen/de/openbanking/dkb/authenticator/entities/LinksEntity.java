package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private LinkDetailsEntity scaOAuth;
    private LinkDetailsEntity scaRedirect;
    private LinkDetailsEntity scaStatus;
    private LinkDetailsEntity self;
    private LinkDetailsEntity startAuthorisation;
    private LinkDetailsEntity startAuthorisationWithAuthenticationMethodSelection;
    private LinkDetailsEntity startAuthorisationWithEncryptedPsuAuthentication;
    private LinkDetailsEntity startAuthorisationWithPsuAuthentication;
    private LinkDetailsEntity startAuthorisationWithPsuIdentification;
    private LinkDetailsEntity startAuthorisationWithTransactionAuthorisation;
    private LinkDetailsEntity status;
}
