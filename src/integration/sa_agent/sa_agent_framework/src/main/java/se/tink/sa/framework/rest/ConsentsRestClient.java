package se.tink.sa.framework.rest;

public interface ConsentsRestClient {

    <Q, S> S checkConsentStatus(Q request);
}
