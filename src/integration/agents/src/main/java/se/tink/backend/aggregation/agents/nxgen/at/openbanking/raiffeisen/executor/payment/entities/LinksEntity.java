package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.executor.payment.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private Href scaRedirect;
    private String scaOAuth;
    private String startAuthorisation;
    private String startAuthorisationWithPsuIdentification;
    private String startAuthorisationWithPsuAuthentication;
    private String startAuthorisationWithEncryptedPsuAuthentication;
    private String startAuthorisationWithAuthenticationMethodSelection;
    private String startAuthorisationWithTransactionAuthorisation;
    private Href self;
    private Href status;
    private String scaStatus;
}
