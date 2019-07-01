package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class NordeaSeApiClient extends NordeaBaseApiClient {

    private String tppToken;
    private String orderRef;

    public NordeaSeApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        super(client, sessionStorage);
    }

    private RequestBuilder createRequestWithTppToken(URL url) {
        String token = getTppToken();
        return createRequest(url)
                .header(
                        NordeaBaseConstants.HeaderKeys.AUTHORIZATION,
                        NordeaSeConstants.HeaderValues.TOKEN_TYPE + " " + token);
    }

    public AuthorizeResponse authorize(AuthorizeRequest authorizeRequest) {
        return createRequest(NordeaSeConstants.Urls.AUTHORIZE)
                .post(AuthorizeResponse.class, authorizeRequest);
    }

    public HttpResponse getCode() {
        return createRequestWithTppToken(new URL(NordeaSeConstants.Urls.GET_CODE + getOrderRef()))
                .get(HttpResponse.class);
    }

    public OAuth2Token getToken(GetTokenForm form) {
        return createRequestWithTppToken(NordeaSeConstants.Urls.GET_TOKEN)
                .body(form, MediaType.APPLICATION_FORM_URLENCODED)
                .post(GetTokenResponse.class)
                .toTinkToken();
    }

    private String getTppToken() {
        return tppToken;
    }

    private String getOrderRef() {
        return orderRef;
    }

    public void setTppToken(String tppToken) {
        this.tppToken = tppToken;
    }

    public void setOrderRef(String orderRef) {
        this.orderRef = orderRef;
    }
}
