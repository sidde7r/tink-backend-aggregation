package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator;

import java.util.Arrays;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.NordeaSeConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.GetCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaSeAuthenticator implements Authenticator {
    private final NordeaSeApiClient apiClient;
    private final SessionStorage sessionStorage;

    public NordeaSeAuthenticator(NordeaSeApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        AuthorizeRequest authorizeRequest = getAuthorizeRequest();

        AuthorizeResponse authorizeResponse = apiClient.authorize(authorizeRequest);

        saveTppToken(authorizeResponse);
        saveOrderRef(authorizeResponse);

        GetCodeResponse getCodeResponse = apiClient.getCode();

        GetTokenForm form = getGetTokenForm(getCodeResponse);

        OAuth2Token accessToken = apiClient.getToken(form);
        apiClient.setTokenToSession(accessToken);
    }

    private GetTokenForm getGetTokenForm(GetCodeResponse getCodeResponse) {
        return GetTokenForm.builder()
                .setCode(getCodeResponse.getResponse().getCode())
                .setGrantType(NordeaBaseConstants.FormValues.AUTHORIZATION_CODE)
                .setRedirectUri(apiClient.getConfiguration().getRedirectUrl())
                .build();
    }

    private AuthorizeRequest getAuthorizeRequest() {
        return new AuthorizeRequest(
                NordeaSeConstants.FormValues.DURATION,
                NordeaSeConstants.FormValues.PSU_ID,
                apiClient.getConfiguration().getRedirectUrl(),
                NordeaSeConstants.FormValues.RESPONSE_TYPE,
                Arrays.asList(
                        NordeaSeConstants.FormValues.ACCOUNTS_BALANCES,
                        NordeaSeConstants.FormValues.ACCOUNTS_BASIC,
                        NordeaSeConstants.FormValues.ACCOUNTS_DETAILS,
                        NordeaSeConstants.FormValues.ACCOUNTS_TRANSACTIONS,
                        NordeaSeConstants.FormValues.PAYMENTS_MULTIPLE),
                NordeaSeConstants.FormValues.STATE);
    }

    private void saveTppToken(AuthorizeResponse authorizeResponse) {
        sessionStorage.put(
                NordeaSeConstants.StorageKeys.TPP_TOKEN,
                authorizeResponse.getResponse().getTppToken());
    }

    private void saveOrderRef(AuthorizeResponse authorizeResponse) {
        sessionStorage.put(
                NordeaSeConstants.StorageKeys.ORDER_REF,
                authorizeResponse.getResponse().getOrderRef());
    }
}
