package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.authenticator;

import com.google.common.base.Strings;
import java.util.Map;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.NordeaFiApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.NordeaBaseAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class NordeaFiAuthenticator extends NordeaBaseAuthenticator {

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
                callbackData.getOrDefault(NordeaBaseConstants.CallbackParams.HTTP_MESSAGE, null);

        if (Strings.isNullOrEmpty(code)) {
            if (Strings.isNullOrEmpty(httpMessage)) {
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
