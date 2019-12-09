package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private HrefEntity scaRedirect;
    private String scaOAuth;
    private String startAuthorisation;
    private String startAuthorisationWithPsuIdentification;
    private String startAuthorisationWithPsuAuthentication;
    private String startAuthorisationWithEncryptedPsuAuthentication;
    private String startAuthorisationWithAuthenticationMethodSelection;
    private String startAuthorisationWithTransactionAuthorisation;
    private HrefEntity self;
    private HrefEntity status;
    private String scaStatus;
}
