package se.tink.backend.aggregation.agents.nxgen.de.openbanking.comdirect;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.ErrorMessages;

public class ComdirectConstants {

    public static final String INTEGRATION_NAME = "comdirect";
    public static final String XS2A_URI_REPLACEMENT = "xs2a-api.comdirect.de";
    public static final String PSD_URI_TO_BE_REPLACED = "psd.comdirect.de";
    public static final String CREDIT_CARD = "Prepaid-Kreditkarte";
    public static final String BASE_URL = "https://xs2a-api.comdirect.de";

    private ComdirectConstants() {
        throw new AssertionError(ErrorMessages.MISSING_AUTHENTICATOR);
    }
}
