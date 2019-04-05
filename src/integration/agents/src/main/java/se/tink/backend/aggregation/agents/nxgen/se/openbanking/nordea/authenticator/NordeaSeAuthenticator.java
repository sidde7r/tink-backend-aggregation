package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator;

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
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.agents.rpc.Credentials;

import java.util.Arrays;

public class NordeaSeAuthenticator implements Authenticator {
    private final NordeaSeApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    public NordeaSeAuthenticator(
            NordeaSeApiClient apiClient,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
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
                .setRedirectUri(persistentStorage.get(NordeaBaseConstants.StorageKeys.REDIRECT_URI))
                .build();
    }

    private AuthorizeRequest getAuthorizeRequest() {
        return new AuthorizeRequest(
                NordeaSeConstants.FormValues.DURATION,
                NordeaSeConstants.FormValues.PSU_ID,
                persistentStorage.get(NordeaBaseConstants.StorageKeys.REDIRECT_URI),
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
