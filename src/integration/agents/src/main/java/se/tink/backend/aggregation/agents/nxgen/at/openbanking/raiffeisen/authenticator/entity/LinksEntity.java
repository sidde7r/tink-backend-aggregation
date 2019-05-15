package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.entity;

import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.entity.common.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private LinkEntity scaRedirect;
    private LinkEntity scaOAuth;
    private LinkEntity startAuthorisation;
    private LinkEntity startAuthorisationWithPsuIdentification;
    private LinkEntity startAuthorisationWithPsuAuthentication;
    private LinkEntity startAuthorisationWithEncryptedPsuAuthentication;
    private LinkEntity startAuthorisationWithAuthenticationMethodSelection;
    private LinkEntity startAuthorisationWithTransactionAuthorisation;
    private LinkEntity self;
    private LinkEntity status;
    private LinkEntity scaStatus;
}
