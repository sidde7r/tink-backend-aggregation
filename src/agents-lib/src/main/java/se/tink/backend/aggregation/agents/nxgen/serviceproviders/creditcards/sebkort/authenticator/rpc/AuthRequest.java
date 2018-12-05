package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class AuthRequest extends AbstractForm {
    public AuthRequest(String uid, String secret, SebKortConfiguration config) {
        super();

        this.put(SebKortConstants.FormKey.SEB_REFERER, SebKortConstants.FormValue.SEB_REFERER);
        this.put(SebKortConstants.FormKey.UID, uid);
        this.put(
                SebKortConstants.FormKey.TARGET,
                String.format(SebKortConstants.FormValue.TARGET, config.getProviderCode()));
        this.put(SebKortConstants.FormKey.TYPE, SebKortConstants.FormValue.TYPE);
        this.put(SebKortConstants.FormKey.SECRET, secret);
        this.put(
                SebKortConstants.FormKey.SEB_AUTH_MECHANISM,
                SebKortConstants.FormValue.SEB_AUTH_MECHANISM);
    }
}
