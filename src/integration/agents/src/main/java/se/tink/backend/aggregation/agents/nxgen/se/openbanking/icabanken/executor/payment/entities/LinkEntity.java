package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkEntity {
    private Href scaRedirect;
    private Href scaOAuth;
    private Href startAuthorisation;
    private Href startAuthorisationWithPsuIdentification;
    private Href updatePsuIdentification;
    private Href startAuthorisationWithProprietaryData;
    private Href updateProprietaryData;
    private Href startAuthorisationWithPsuAuthentication;
    private Href updatePsuAuthentication;
    private Href startAuthorisationWithEncryptedPsuAuthentication;
    private Href updateEncryptedPsuAuthentication;
    private Href selectAuthenticationMethod;
    private Href startAuthorisationWithTransactionAuthorisation;
    private Href self;
    private Href status;
    private Href scaStatus;
    private Href account;
    private Href balances;
    private Href transactions;
    private Href transactionDetails;
    private Href first;
    private Href next;
    private Href previous;
    private Href last;
    private Href download;
}
