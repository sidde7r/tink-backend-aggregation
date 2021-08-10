package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import java.util.concurrent.TimeUnit;

public abstract class OpenIdAuthenticatorConstants {

    public static final String CONSENT_ERROR_OCCURRED = "UNSPECIFIED_CONSENT_ID";

    /** According to examples the max age is 24h */
    public static final long MAX_AGE = TimeUnit.DAYS.toSeconds(90);

    /**
     * "To indicate that secure customer authentication must be carried out as mandated by the PSD2
     * RTS"
     */
    public static final String ACR_SECURE_AUTHENTICATION_RTS = "urn:openbanking:psd2:sca";

    public static class Params {
        public static final String REQUEST = "request";
        public static final String MAX_AGE = "max_age";
        public static final String CLAIMS = "claims";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String REDIRECT_URI = "redirect_uri";
    }
}
