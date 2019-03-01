package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco;

import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.EELoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.EELoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.KeepAliveRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.rpc.GlobalPositionFirstTimeResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.Map;

public class EvoBancoApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;

    public EvoBancoApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public LoginResponse login(LoginRequest loginRequest) throws LoginException {

        try {
            return createRequest(EvoBancoConstants.Urls.LOGIN)
                    .body(loginRequest, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                    .post(LoginResponse.class);
        } catch (HttpResponseException e) {
            int statusCode = e.getResponse().getStatus();

            if (statusCode == EvoBancoConstants.StatusCodes.INCORRECT_USERNAME_PASSWORD) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            throw e;
        }
    }

    public EELoginResponse eeLogin(EELoginRequest eeLoginRequest)
            throws LoginException, SessionException {

        Map<String, Object> headers = new HashMap<>();
        headers.put(EvoBancoConstants.HeaderKeys.COD_SEC_USER, "");
        headers.put(EvoBancoConstants.HeaderKeys.COD_SEC_TRANS, "");
        headers.put(EvoBancoConstants.HeaderKeys.COD_TERMINAL, "");
        headers.put(
                EvoBancoConstants.HeaderKeys.COD_CANAL, EvoBancoConstants.HeaderValues.COD_CANAL);
        headers.put(EvoBancoConstants.HeaderKeys.COD_APL, EvoBancoConstants.HeaderValues.COD_APL);
        headers.put(
                EvoBancoConstants.HeaderKeys.COD_SEC_IP, EvoBancoConstants.HeaderValues.COD_SEC_IP);
        headers.put(
                EvoBancoConstants.HeaderKeys.COD_SEC_ENT,
                sessionStorage.get(EvoBancoConstants.Storage.ENTITY_CODE));

        try {

            HttpResponse response = createRequest(EvoBancoConstants.Urls.EE_LOGIN)
                    .headers(headers)
                    .post(HttpResponse.class, eeLoginRequest);

            setNextCodSecIpHeader(response);

            return response.getBody(EELoginResponse.class);

        } catch (HttpResponseException e) {
            int statusCode = e.getResponse().getStatus();

            if (statusCode == EvoBancoConstants.StatusCodes.INCORRECT_USERNAME_PASSWORD) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            throw e;
        }
    }

    public boolean isAlive(KeepAliveRequest keepAliveRequest) throws SessionException {

        try {
            HttpResponse response = createRequest(EvoBancoConstants.Urls.KEEP_ALIVE)
                    .headers(getEEHeaders())
                    .post(HttpResponse.class, keepAliveRequest);

            setNextCodSecIpHeader(response);
        } catch (HttpResponseException e) {
            return false;
        }

        return true;
    }

    public GlobalPositionFirstTimeResponse globalPositionFirstTime() throws SessionException {

        HttpResponse response = createRequest(EvoBancoConstants.Urls.GLOBAL_POSITION_FIRST_TIME)
                .headers(getEEHeaders())
                .queryParam(EvoBancoConstants.QueryParamsKeys.AGREEMENT_BE, sessionStorage.get(EvoBancoConstants.Storage.AGREEMENT_BE))
                .queryParam(EvoBancoConstants.QueryParamsKeys.ENTITY_CODE, sessionStorage.get(EvoBancoConstants.Storage.ENTITY_CODE))
                .queryParam(EvoBancoConstants.QueryParamsKeys.INTERNAL_ID_PE, sessionStorage.get(EvoBancoConstants.Storage.INTERNAL_ID_PE))
                .queryParam(EvoBancoConstants.QueryParamsKeys.USER_BE, sessionStorage.get(EvoBancoConstants.Storage.USER_BE))
                .get(HttpResponse.class);

        setNextCodSecIpHeader(response);

        return response.getBody(GlobalPositionFirstTimeResponse.class);
    }

    private Map<String, Object> getEEHeaders() throws SessionException {
        Map<String, Object> headers = new HashMap<>();
        headers.put(EvoBancoConstants.HeaderKeys.COD_SEC_USER, "");
        headers.put(EvoBancoConstants.HeaderKeys.COD_SEC_TRANS, "");
        headers.put(EvoBancoConstants.HeaderKeys.COD_TERMINAL, "");
        headers.put(
                EvoBancoConstants.HeaderKeys.COD_CANAL, EvoBancoConstants.HeaderValues.COD_CANAL);
        headers.put(EvoBancoConstants.HeaderKeys.COD_APL, EvoBancoConstants.HeaderValues.COD_APL);
        headers.put(
                EvoBancoConstants.HeaderKeys.COD_SEC_IP,
                sessionStorage
                        .get(EvoBancoConstants.Storage.COD_SEC_IP, String.class)
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception));
        headers.put(
                EvoBancoConstants.HeaderKeys.COD_SEC_ENT,
                sessionStorage.get(EvoBancoConstants.Storage.ENTITY_CODE));

        return headers;
    }

    private void setNextCodSecIpHeader(HttpResponse response) {
        MultivaluedMap<String, String> responseHeaders = response.getHeaders();
        sessionStorage.put(
                EvoBancoConstants.Storage.COD_SEC_IP,
                responseHeaders.getFirst(EvoBancoConstants.HeaderKeys.COD_SEC_IP));
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }
}
