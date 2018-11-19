package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.ws.rs.core.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.entities.RootModel;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.entities.ValueEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.rpc.AuthenticationMeansRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.rpc.AuthenticationProcessRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.rpc.EBankingUsersRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.rpc.GenerateChallangeRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class FortisApiClient {
    private final SessionStorage sessionStorage;
    private final TinkHttpClient client;

    public FortisApiClient(SessionStorage sessionStorage, TinkHttpClient client) {
        this.sessionStorage = sessionStorage;
        this.client = client;
    }

    private static URL getUrl(String resource) {
        return new URL(FortisConstants.URLS.HOST + resource);
    }

    private RequestBuilder firstRequest() {
        Cookie CSRFCookie =
                new Cookie(FortisConstants.HEADERS.CSRF, FortisConstants.HEADER_VALUES.CSRF_VALUE);
        Cookie axesCookie =
                new Cookie(FortisConstants.HEADERS.DEVICE_FEATURES, FortisConstants.HEADER_VALUES.DEVICE_FEATURES_VALUE);
        Cookie deviceFeaturesCookie =
                new Cookie(FortisConstants.HEADERS.AXES, FortisConstants.HEADER_VALUES.AXES_VALUE);
        Cookie distributorIdCookie =
                new Cookie(FortisConstants.HEADERS.DISTRIBUTOR_ID, FortisConstants.HEADER_VALUES.DISTRIBUTOR_ID_VALUE);
        Cookie europolicyCookie =
                new Cookie(FortisConstants.HEADERS.EURO_POLICY, FortisConstants.HEADER_VALUES.EURO_POLICY_VALUE);

        RequestBuilder requestBuilder = client.request(getUrl(FortisConstants.URLS.GET_DISTRIBUTOR_AUTHENTICATION_MEANS))
                .header(FortisConstants.HEADERS.CONTENT_TYPE, FortisConstants.HEADER_VALUES.CONTENT_TYPE_VALUE)
                .header(FortisConstants.HEADERS.CSRF, FortisConstants.HEADER_VALUES.CSRF_VALUE)
                .accept("*/*")
                .cookie(CSRFCookie)
                .cookie(axesCookie)
                .cookie(deviceFeaturesCookie)
                .cookie(distributorIdCookie)
                .cookie(europolicyCookie);

         return requestBuilder;
    }

    private RequestBuilder makeRequest(String resource) {
        return client.request(getUrl(resource))
                .header(FortisConstants.HEADERS.CONTENT_TYPE, FortisConstants.HEADER_VALUES.CONTENT_TYPE_VALUE)
                .header(FortisConstants.HEADERS.CSRF, FortisConstants.HEADER_VALUES.CSRF_VALUE);
    }

    public ValueEntity getEBankingUsers(EBankingUsersRequest eBankingUsersRequest) throws JsonProcessingException {
        AuthenticationMeansRequest meansRequest = new AuthenticationMeansRequest("","49", "3","49FB001");
        String serialized = new ObjectMapper().writeValueAsString(meansRequest);
        ValueEntity valueEntity = firstRequest().post(RootModel.class, serialized).getValue();
        sessionStorage.put("MEAN_ID", valueEntity.getDistributorAuthenticationMeans().get(0).getAuthenticationMeanId());
        String serialized2 = new ObjectMapper().writeValueAsString(eBankingUsersRequest);
        return makeRequest(FortisConstants.URLS.GET_E_BANKING_USERS).post(ValueEntity.class, serialized2);
    }

    public HttpResponse createAuthenticationProcess(AuthenticationProcessRequest authenticationProcessRequest) throws JsonProcessingException {
        String serialized = new ObjectMapper().writeValueAsString(authenticationProcessRequest);
        return makeRequest(FortisConstants.URLS.CREATE_AUTHENTICATION_PROCESS).post(HttpResponse.class, serialized);
    }

    public String generateChallanges(GenerateChallangeRequest challangeRequest) throws JsonProcessingException {
        String serialized = new ObjectMapper().writeValueAsString(challangeRequest);
        return makeRequest(FortisConstants.URLS.GENERATE_CHALLENGES).post(HttpResponse.class, serialized).getBody(
                ValueEntity.class).getChallenges().get(0);
    }

    public HttpResponse getUserInfo() {
        return makeRequest(FortisConstants.URLS.CREATE_AUTHENTICATION_PROCESS).post(HttpResponse.class);
    }

    public void authenticationRequest(String loginChallenge) {
        makeRequest(FortisConstants.URLS.AUTHENTICATION_URL).post(loginChallenge);
    }
}
