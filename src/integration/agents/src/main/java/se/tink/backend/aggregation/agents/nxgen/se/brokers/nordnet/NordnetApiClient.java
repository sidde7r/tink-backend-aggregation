package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet;

import com.google.common.base.Strings;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc.CustomerInfoResponse;
import se.tink.backend.aggregation.constants.CommonHeaders;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.identitydata.IdentityData;

public class NordnetApiClient {

    private final TinkHttpClient client;
    private String referrer;

    public NordnetApiClient(TinkHttpClient client) {
        this.client = client;
    }

    private RequestBuilder createRequest(String url) {
        return createBasicRequest(url)
                .header(HttpHeaders.CONNECTION, NordnetConstants.HeaderValues.KEEP_ALIVE)
                .header(NordnetConstants.HeaderKeys.REFERRER, referrer)
                .type(MediaType.APPLICATION_JSON_TYPE);
    }

    public RequestBuilder createBasicRequest(String url) {
        return client.request(url)
                .header(HttpHeaders.USER_AGENT, CommonHeaders.DEFAULT_USER_AGENT)
                .accept(
                        MediaType.TEXT_HTML,
                        MediaType.APPLICATION_XHTML_XML,
                        NordnetConstants.HeaderKeys.APPLICATION_XML_Q,
                        NordnetConstants.HeaderKeys.GENERIC_MEDIA_TYPE);
    }

    public <T> T get(String loginBankidPageUrl, Class<T> responseClass) {
        return createRequest(loginBankidPageUrl).get(responseClass);
    }

    public <T> T get(RequestBuilder requestBuilder, Class<T> responseClass) {
        return requestBuilder.get(responseClass);
    }

    public <T, R> T post(String url, Class<T> responseClass, R body) {
        return createRequest(url).post(responseClass, body);
    }

    public <T> T post(RequestBuilder requestBuilder, Class<T> responseClass) {
        return requestBuilder.post(responseClass);
    }

    public boolean authorizeSession(OAuth2Token token) {
        try {
            createBasicRequest(NordnetConstants.Urls.ENVIRONMENT_URL)
                    .addBearerToken(token)
                    .get(HttpResponse.class);
            return true;
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED
                    && e.getResponse()
                            .getBody(String.class)
                            .contains(NordnetConstants.Errors.INVALID_SESSION)) {
                return false;
            }

            // Re-throw unknown exception
            throw e;
        }
    }

    public IdentityData fetchIdentityData() {
        CustomerInfoResponse customerInfo =
                createBasicRequest(NordnetConstants.Urls.GET_CUSTOMER_INFO_URL)
                        .get(CustomerInfoResponse.class);
        return customerInfo.toTinkIdentity();
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public void setNextReferrer(MultivaluedMap<String, String> headers) {
        String nextReferrer = headers.getFirst(NordnetConstants.HeaderKeys.LOCATION);

        if (!Strings.isNullOrEmpty(nextReferrer)) {
            referrer = NordnetConstants.Urls.BASE_URL + nextReferrer;
        }
    }
}
