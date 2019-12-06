package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc;

public interface ConsentResponse {

    String getConsentId();

    String getScaRedirect();
}
