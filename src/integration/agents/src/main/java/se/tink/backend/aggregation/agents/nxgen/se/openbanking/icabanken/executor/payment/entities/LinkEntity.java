package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkEntity {
    private HrefEntity scaRedirect;
    private HrefEntity scaOAuth;
    private HrefEntity startAuthorisation;
    private HrefEntity startAuthorisationWithPsuIdentification;
    private HrefEntity updatePsuIdentification;
    private HrefEntity startAuthorisationWithProprietaryData;
    private HrefEntity updateProprietaryData;
    private HrefEntity startAuthorisationWithPsuAuthentication;
    private HrefEntity updatePsuAuthentication;
    private HrefEntity startAuthorisationWithEncryptedPsuAuthentication;
    private HrefEntity updateEncryptedPsuAuthentication;
    private HrefEntity selectAuthenticationMethod;
    private HrefEntity startAuthorisationWithTransactionAuthorisation;
    private HrefEntity self;
    private HrefEntity status;
    private HrefEntity scaStatus;
    private HrefEntity account;
    private HrefEntity balances;
    private HrefEntity transactions;
    private HrefEntity transactionDetails;
    private HrefEntity first;
    private HrefEntity next;
    private HrefEntity previous;
    private HrefEntity last;
    private HrefEntity download;
}
