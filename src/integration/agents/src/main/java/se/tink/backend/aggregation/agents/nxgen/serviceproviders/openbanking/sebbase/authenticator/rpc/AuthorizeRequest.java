package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.QueryValues;
import se.tink.backend.aggregation.nxgen.http.form.Form;

public class AuthorizeRequest {

    private String consentFormVerifier;

    public AuthorizeRequest(String consentFormVerifier) {
        this.consentFormVerifier = consentFormVerifier;
    }

    public String toData() {
        return Form.builder()
                .put(QueryKeys.SCOPE, QueryValues.SCOPE)
                .put(QueryKeys.TRUST_LEVEL, QueryValues.PERMIT)
                .put(QueryKeys.CONSENT_FORM_VERIFIER, consentFormVerifier)
                .build()
                .serialize();
    }
}
