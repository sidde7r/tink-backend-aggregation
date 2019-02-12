package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class LoginRequest extends AbstractForm {
    public LoginRequest(String saml, SebKortConfiguration config) {
        super();

        this.put(SebKortConstants.FormKey.COUNTRY_CODE, SebKortConstants.FormValue.COUNTRY_CODE);
        this.put(SebKortConstants.FormKey.TARGET_URL, SebKortConstants.FormValue.TARGET_URL);
        this.put(SebKortConstants.FormKey.SEB_REFERER, SebKortConstants.FormValue.SEB_REFERER);
        this.put(SebKortConstants.FormKey.SAML_RESPONSE, saml);
        this.put(SebKortConstants.FormKey.PRODGROUP, config.getProductCode());
    }
}
