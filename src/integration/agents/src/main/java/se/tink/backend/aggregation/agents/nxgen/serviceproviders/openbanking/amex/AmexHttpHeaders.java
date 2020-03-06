package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AmexHttpHeaders {

    public static final String X_AMEX_API_KEY = "x-amex-api-key";
    public static final String X_AMEX_REQUEST_ID = "x-amex-request-id";
    public static final String AUTHENTICATION = "Authentication";
}
