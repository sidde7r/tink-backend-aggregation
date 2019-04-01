package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordease;

import javax.ws.rs.core.MediaType;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordease.authenticator.rpc.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordease.authenticator.rpc.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordease.authenticator.rpc.GetCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordease.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.*;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

import java.util.Arrays;

public final class NordeaSeApiClient extends NordeaBaseApiClient {

    public NordeaSeApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        super(client, sessionStorage, persistentStorage);
    }

    private RequestBuilder createRequestWithTppToken(URL url) {
        String token = getTppToken();
        return createRequest(url)
                .header(
                        NordeaBaseConstants.HeaderKeys.AUTHORIZATION,
                        NordeaSeConstants.HeaderValues.TOKEN_TYPE + " " + token);
    }

    public AuthorizeResponse authorize(AuthorizeRequest authorizeRequest) {
        AuthorizeResponse res =
                createRequest(new URL(NordeaSeConstants.Urls.AUTHORIZE))
                        .post(AuthorizeResponse.class, authorizeRequest);

        return res;
    }

    public GetCodeResponse getCode() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return createRequestWithTppToken(
                        new URL(NordeaSeConstants.Urls.GET_CODE + getOrderRef()))
                .get(GetCodeResponse.class);
    }

    public OAuth2Token getToken(GetTokenForm form) {
        return createRequestWithTppToken(new URL(NordeaSeConstants.Urls.GET_TOKEN))
                .body(form, MediaType.APPLICATION_FORM_URLENCODED)
                .post(GetTokenResponse.class)
                .toTinkToken();
    }

    private String getTppToken() {
        return sessionStorage.get(NordeaSeConstants.StorageKeys.TPP_TOKEN);
    }

    private String getOrderRef() {
        return sessionStorage.get(NordeaSeConstants.StorageKeys.ORDER_REF);
    }
}
