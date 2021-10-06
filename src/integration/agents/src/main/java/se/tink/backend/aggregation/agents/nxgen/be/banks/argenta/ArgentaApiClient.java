package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta;

import com.google.common.base.Strings;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.ConfigResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.StartAuthRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.StartAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.ValidateAuthRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.rpc.ArgentaAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.rpc.ArgentaTransactionResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class ArgentaApiClient {

    private final TinkHttpClient client;
    private final ArgentaSessionStorage sessionStorage;

    void keepAlive(String deviceId) {
        RequestBuilder request = client.request(ArgentaConstants.Url.KEEP_ALIVE_URL);
        addMandatoryHeaders(request, deviceId);
        setAuthorization(request);
        HttpResponse response = request.post(HttpResponse.class);
        if (response.getStatus() == 204) {
            storeAuthorization(response);
            return;
        }
        throw SessionError.SESSION_EXPIRED.exception();
    }

    public ConfigResponse getConfig(URL config, String deviceToken) {
        RequestBuilder request =
                client.request(config)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE);
        addMandatoryHeaders(request, deviceToken);
        return getRequestWithAuthorization(ConfigResponse.class, request);
    }

    public StartAuthResponse startAuth(
            URL authStart, StartAuthRequest registrationRequest, String deviceToken) {
        RequestBuilder request =
                client.request(authStart)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE);
        addMandatoryHeaders(request, deviceToken);
        return postRequestWithAuthorization(request, registrationRequest)
                .getBody(StartAuthResponse.class);
    }

    public HttpResponse validateAuth(ValidateAuthRequest validateAuthRequest, String deviceToken) {
        RequestBuilder request =
                client.request(ArgentaConstants.Url.AUTH_VALIDATE)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .type(MediaType.APPLICATION_JSON_TYPE);
        addMandatoryHeaders(request, deviceToken);
        return postRequestWithAuthorization(request, validateAuthRequest);
    }

    public ArgentaAccountResponse fetchAccounts(int page, String deviceId) {
        RequestBuilder request =
                client.request(ArgentaConstants.Url.ACCOUNTS)
                        .accept(MediaType.APPLICATION_JSON_TYPE);

        if (page > 1) request.queryParam(ArgentaConstants.PARAMETERS.PAGE, Integer.toString(page));

        addMandatoryHeaders(request, deviceId);
        return getRequestWithAuthorization(ArgentaAccountResponse.class, request);
    }

    public ArgentaTransactionResponse fetchTransactions(
            String accountId, int page, String deviceId) {
        URL transactions =
                ArgentaConstants.Url.ACCOUNTS
                        .concat("/")
                        .concat(accountId)
                        .concat(ArgentaConstants.Url.TRANSACTIONS);
        RequestBuilder request =
                client.request(transactions).accept(MediaType.APPLICATION_JSON_TYPE);
        request.queryParam(ArgentaConstants.PARAMETERS.PAGE, Integer.toString(page));
        addMandatoryHeaders(request, deviceId);
        return getRequestWithAuthorization(ArgentaTransactionResponse.class, request);
    }

    void logOff(String deviceId) {
        RequestBuilder request = client.request(ArgentaConstants.Url.LOG_OFF);
        addMandatoryHeaders(request, deviceId);
        setAuthorization(request);
        request.post(HttpResponse.class);
    }

    private void addMandatoryHeaders(RequestBuilder builder, String deviceId) {
        for (Map.Entry<String, String> header : ArgentaConstants.HEADERS.entrySet()) {
            builder.header(header.getKey(), header.getValue());
        }
        builder.header(ArgentaConstants.HEADER.DEVICE_ID, deviceId);
    }

    private void addAuthorizationHeader(RequestBuilder builder, String authorization) {
        builder.header(ArgentaConstants.HEADER.AUTHORIZATION, "Bearer " + authorization);
    }

    private <T> HttpResponse postRequestWithAuthorization(RequestBuilder request, T post) {
        setAuthorization(request);
        HttpResponse response = request.post(HttpResponse.class, post);
        storeAuthorization(response);
        return response;
    }

    private void setAuthorization(RequestBuilder request) {
        if (!Strings.isNullOrEmpty(sessionStorage.getAuthorization()))
            addAuthorizationHeader(request, sessionStorage.getAuthorization());
    }

    private <T> T getRequestWithAuthorization(Class<T> responseClass, RequestBuilder request) {
        setAuthorization(request);
        HttpResponse response = request.get(HttpResponse.class);
        return response.getBody(responseClass);
    }

    private void storeAuthorization(HttpResponse response) {
        String authorization =
                response.getHeaders().getFirst(ArgentaConstants.HEADER.AUTHORIZATION);
        sessionStorage.setAuthorization(authorization);
    }
}
