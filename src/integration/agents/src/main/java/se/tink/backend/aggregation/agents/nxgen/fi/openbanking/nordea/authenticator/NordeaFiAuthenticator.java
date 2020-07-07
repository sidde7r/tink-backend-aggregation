package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.authenticator;

import com.google.common.base.Strings;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.NordeaFiApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.NordeaBaseAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class NordeaFiAuthenticator extends NordeaBaseAuthenticator {
    private static final Logger logger = LoggerFactory.getLogger(NordeaFiAuthenticator.class);

    private static final String COUNTRY = "FI";

    public NordeaFiAuthenticator(NordeaFiApiClient apiClient) {
        super(apiClient);
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(state, COUNTRY);
    }

    @Override
    public void handleSpecificCallbackDataError(Map<String, String> callbackData)
            throws AuthenticationException {

        final String code = callbackData.getOrDefault(OAuth2Constants.CallbackParams.CODE, null);
        final String httpMessage =
                callbackData.getOrDefault(
                        // this one is for running test locally
                        NordeaBaseConstants.CallbackParams.HTTP_MESSAGE,
                        // this one is for running in production
                        callbackData.getOrDefault(
                                NordeaBaseConstants.CallbackParams.HTTP_MESSAGE.toLowerCase(),
                                null));

        if (Strings.isNullOrEmpty(code)) {
            if (Strings.isNullOrEmpty(httpMessage)) {

                // no valid callbackData was found. Will log the callbackData keys
                logger.info(String.format("callbackData keys: %s", callbackData.keySet()));

                throw new IllegalStateException(
                        "callbackData did not contain 'code' or 'httpMessage'");
            }
            if (httpMessage.equalsIgnoreCase(NordeaBaseConstants.ErrorCodes.SESSION_CANCELLED)) {
                throw ThirdPartyAppError.CANCELLED.exception();
            }
            throw new IllegalStateException(
                    String.format("Unknown callbackData for 'httpMessage': %s", httpMessage));
        }
    }
}
