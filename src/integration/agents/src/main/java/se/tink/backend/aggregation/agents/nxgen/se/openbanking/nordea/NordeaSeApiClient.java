package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.RefreshTokenForm;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class NordeaSeApiClient extends NordeaBaseApiClient {
    private final PersistentStorage persistentStorage;

    public NordeaSeApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        super(client, sessionStorage);
        this.persistentStorage = persistentStorage;
    }

    private RequestBuilder createRequestWithTppToken(URL url, String token) {
        return createRequest(url)
                .header(
                        NordeaBaseConstants.HeaderKeys.AUTHORIZATION,
                        NordeaSeConstants.HeaderValues.TOKEN_TYPE + " " + token);
    }

    public AuthorizeResponse authorize(AuthorizeRequest authorizeRequest) {
        return createRequest(NordeaSeConstants.Urls.AUTHORIZE)
                .post(AuthorizeResponse.class, authorizeRequest);
    }

    public HttpResponse getCode(String orderRef, String token) {
        return createRequestWithTppToken(new URL(NordeaSeConstants.Urls.GET_CODE + orderRef), token)
                .get(HttpResponse.class);
    }

    public OAuth2Token getToken(GetTokenForm form, String token) {
        return createRequestWithTppToken(NordeaSeConstants.Urls.GET_TOKEN, token)
                .body(form, MediaType.APPLICATION_FORM_URLENCODED)
                .post(GetTokenResponse.class)
                .toTinkToken();
    }

    @Override
    public OAuth2Token refreshToken(RefreshTokenForm form) {
        return createRequest(NordeaSeConstants.Urls.GET_TOKEN)
                .body(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(GetTokenResponse.class)
                .toTinkToken();
    }

    @Override
    public OAuth2Token getStoredToken() {
        return persistentStorage
                .get(OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find token!"));
    }

    @Override
    public void storeToken(OAuth2Token token) {
        persistentStorage.put(OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN, token);
    }
}
