package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco;

import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities.EELoginAnswerEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.EELoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.EELoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.KeepAliveRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.LinkingLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.LinkingLoginResponse1;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.LinkingLoginResponse2;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.rpc.GlobalPositionFirstTimeResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.rpc.GlobalPositionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.rpc.TransactionsPaginationRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.rpc.TransactionsResponse;
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

    public LinkingLoginResponse1 link1(LinkingLoginRequest linkingLoginRequest)
            throws LoginException {
        try {
            HttpResponse response =
                    createRequest(EvoBancoConstants.Urls.LINKING_LOGIN)
                            .headers(getEEHeaders())
                            .post(HttpResponse.class, linkingLoginRequest);

            setNextCodSecIpHeader(response);

            return response.getBody(LinkingLoginResponse1.class);
        } catch (HttpResponseException e) {
            int statusCode = e.getResponse().getStatus();

            if (statusCode == EvoBancoConstants.StatusCodes.BAD_REQUEST_STATUS_CODE) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            throw e;
        }
    }

    public LinkingLoginResponse2 link2(LinkingLoginRequest linkingLoginRequest)
            throws LoginException {
        try {
            HttpResponse response =
                    createRequest(EvoBancoConstants.Urls.LINKING_LOGIN)
                            .headers(getEEHeaders())
                            .post(HttpResponse.class, linkingLoginRequest);

            setNextCodSecIpHeader(response);

            LinkingLoginResponse2 linkingLoginResponse2 =
                    response.getBody(LinkingLoginResponse2.class);

            if (linkingLoginResponse2
                    .getEeOLinkingAndLogin()
                    .getReturnCode()
                    .equals(EvoBancoConstants.ReturnCodes.UNSUCCESSFUL_RETURN_CODE)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
            processResponse(linkingLoginResponse2.getEeOLinkingAndLogin().getAnswer());

            return linkingLoginResponse2;
        } catch (HttpResponseException e) {
            int statusCode = e.getResponse().getStatus();

            if (statusCode == EvoBancoConstants.StatusCodes.BAD_REQUEST_STATUS_CODE) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            throw e;
        }
    }

    public LoginResponse login(LoginRequest loginRequest) throws LoginException {

        try {
            LoginResponse loginResponse =
                    createRequest(EvoBancoConstants.Urls.LOGIN)
                            .body(loginRequest, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                            .post(LoginResponse.class);

            // Needed for other requests
            sessionStorage.put(
                    EvoBancoConstants.Storage.AGREEMENT_BE,
                    loginResponse.getUserinfo().getAgreementBE());
            sessionStorage.put(
                    EvoBancoConstants.Storage.ENTITY_CODE,
                    loginResponse.getUserinfo().getEntityCode());
            sessionStorage.put(
                    EvoBancoConstants.Storage.USER_BE, loginResponse.getUserinfo().getUserBE());
            sessionStorage.put(
                    EvoBancoConstants.Storage.USER_ID,
                    loginResponse.getUserinfo().getMobilePhone());
            sessionStorage.put(
                    EvoBancoConstants.Storage.HOLDER_NAME,
                    loginResponse.getUserinfo().getClientName()
                            + " "
                            + loginResponse.getUserinfo().getSurname1Client()
                            + " "
                            + loginResponse.getUserinfo().getSurname2Client());

            return loginResponse;
        } catch (HttpResponseException e) {
            int statusCode = e.getResponse().getStatus();

            if (statusCode == EvoBancoConstants.StatusCodes.BAD_REQUEST_STATUS_CODE) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            throw e;
        }
    }

    public EELoginResponse eeLogin(EELoginRequest eeLoginRequest) throws LoginException {

        try {

            HttpResponse response =
                    createRequest(EvoBancoConstants.Urls.EE_LOGIN)
                            .headers(getEEHeaders())
                            .post(HttpResponse.class, eeLoginRequest);

            setNextCodSecIpHeader(response);

            EELoginResponse eeLoginResponse = response.getBody(EELoginResponse.class);

            if (eeLoginResponse
                    .getEeOLogin()
                    .getReturnCode()
                    .equals(EvoBancoConstants.ReturnCodes.UNSUCCESSFUL_RETURN_CODE)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            processResponse(eeLoginResponse.getEeOLogin().getAnswer());

            return eeLoginResponse;

        } catch (HttpResponseException e) {
            int statusCode = e.getResponse().getStatus();

            if (statusCode == EvoBancoConstants.StatusCodes.BAD_REQUEST_STATUS_CODE) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            throw e;
        }
    }

    public boolean isAlive(KeepAliveRequest keepAliveRequest) {

        try {
            HttpResponse response =
                    createRequest(EvoBancoConstants.Urls.KEEP_ALIVE)
                            .headers(getEEHeaders())
                            .post(HttpResponse.class, keepAliveRequest);

            setNextCodSecIpHeader(response);
        } catch (HttpResponseException e) {
            return false;
        }

        return true;
    }

    public GlobalPositionFirstTimeResponse globalPositionFirstTime() {

        HttpResponse response =
                createRequest(EvoBancoConstants.Urls.GLOBAL_POSITION_FIRST_TIME)
                        .headers(getEEHeaders())
                        .queryParam(
                                EvoBancoConstants.QueryParamsKeys.AGREEMENT_BE,
                                sessionStorage.get(EvoBancoConstants.Storage.AGREEMENT_BE))
                        .queryParam(
                                EvoBancoConstants.QueryParamsKeys.ENTITY_CODE,
                                sessionStorage.get(EvoBancoConstants.Storage.ENTITY_CODE))
                        .queryParam(
                                EvoBancoConstants.QueryParamsKeys.INTERNAL_ID_PE,
                                sessionStorage.get(EvoBancoConstants.Storage.INTERNAL_ID_PE))
                        .queryParam(
                                EvoBancoConstants.QueryParamsKeys.USER_BE,
                                sessionStorage.get(EvoBancoConstants.Storage.USER_BE))
                        .get(HttpResponse.class);

        setNextCodSecIpHeader(response);

        return response.getBody(GlobalPositionFirstTimeResponse.class);
    }

    public GlobalPositionResponse globalPosition() {
        HttpResponse response =
                createRequest(EvoBancoConstants.Urls.FETCH_ACCOUNTS)
                        .headers(getEEHeaders())
                        .queryParam(
                                EvoBancoConstants.QueryParamsKeys.AGREEMENT_BE,
                                sessionStorage.get(EvoBancoConstants.Storage.AGREEMENT_BE))
                        .queryParam(
                                EvoBancoConstants.QueryParamsKeys.ENTITY_CODE,
                                sessionStorage.get(EvoBancoConstants.Storage.ENTITY_CODE))
                        .queryParam(
                                EvoBancoConstants.QueryParamsKeys.INTERNAL_ID_PE,
                                sessionStorage.get(EvoBancoConstants.Storage.INTERNAL_ID_PE))
                        .queryParam(
                                EvoBancoConstants.QueryParamsKeys.USER_BE,
                                sessionStorage.get(EvoBancoConstants.Storage.USER_BE))
                        .get(HttpResponse.class);

        setNextCodSecIpHeader(response);

        return response.getBody(GlobalPositionResponse.class);
    }

    public TransactionsResponse fetchTransactions(TransactionsPaginationRequest request) {
        HttpResponse response =
                createRequest(EvoBancoConstants.Urls.FETCH_TRANSACTIONS)
                        .headers(getEEHeaders())
                        .post(HttpResponse.class, request);

        setNextCodSecIpHeader(response);

        return response.getBody(TransactionsResponse.class);
    }

    private Map<String, Object> getEEHeaders() {
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
                        .orElse(EvoBancoConstants.HeaderValues.COD_SEC_IP));
        headers.put(
                EvoBancoConstants.HeaderKeys.COD_SEC_ENT,
                sessionStorage.get(EvoBancoConstants.Storage.ENTITY_CODE));

        return headers;
    }

    public void setNextCodSecIpHeader(HttpResponse response) {
        MultivaluedMap<String, String> responseHeaders = response.getHeaders();
        String newCodSecIp = responseHeaders.getFirst(EvoBancoConstants.HeaderKeys.COD_SEC_IP);

        if (newCodSecIp == null || newCodSecIp.isEmpty()) {
            newCodSecIp = EvoBancoConstants.HeaderValues.COD_SEC_IP;
        }

        sessionStorage.put(EvoBancoConstants.Storage.COD_SEC_IP, newCodSecIp);
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    private void processResponse(EELoginAnswerEntity answer) {
        sessionStorage.put(EvoBancoConstants.Storage.INTERNAL_ID_PE, answer.getInternalIdPe());
    }
}
