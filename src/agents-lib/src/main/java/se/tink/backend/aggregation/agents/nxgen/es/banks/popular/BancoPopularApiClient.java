package se.tink.backend.aggregation.agents.nxgen.es.banks.popular;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.commons.codec.binary.Hex;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc.FetchAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc.FetchTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc.SetContractRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc.SetContractResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.sessionhandler.rpc.KeepAliveResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BancoPopularApiClient {
    private static final AggregationLogger LOGGER =
            new AggregationLogger(BancoPopularApiClient.class);

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;

    public BancoPopularApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public LoginResponse login(LoginRequest loginRequest) throws LoginException {
        try {

            HttpResponse rawResponse = createRequest(BancoPopularConstants.Urls.LOGIN_URL,
                    BancoPopularConstants.ApiService.LOGIN_PATH,
                    loginRequest)
                    .post(HttpResponse.class, loginRequest);

            handleResponse(rawResponse);

            String clientIp = rawResponse.getHeaders()
                    .getFirst(BancoPopularConstants.Authentication.HEADER_X_CLIENT_IP);
            setClientIP(clientIp);

            return rawResponse.getBody(LoginResponse.class);

        } catch (HttpResponseException e) {
            int statusCode = e.getResponse().getStatus();

            ifLoginErrorThrowLoginException(statusCode);

            // the bank will respond 500 Internal server error if we use a non-numeric password,
            // but we cannot tell that apart from any other 500 response.
            LOGGER.warn("Login to Popular failed, status code: " + statusCode);
            throw e;
        }
    }

    public SetContractResponse setContract(SetContractRequest request) {
        request.setIp(getClientIP());

        HttpResponse rawResponse = createRequest(BancoPopularConstants.Urls.SET_CONTRACT_URL,
                BancoPopularConstants.ApiService.SET_CONTRACT_PATH,
                request)
                .post(HttpResponse.class, request);

        handleResponse(rawResponse);

        return rawResponse.getBody(SetContractResponse.class);
    }

    public FetchAccountsResponse fetchAccounts(FetchAccountsRequest request) {
        HttpResponse rawResponse = createRequest(BancoPopularConstants.Urls.FETCH_ACCOUNTS_URL,
                BancoPopularConstants.ApiService.ACCOUNTS_PATH,
                request)
                .post(HttpResponse.class, request);

        handleResponse(rawResponse);

        return rawResponse.getBody(FetchAccountsResponse.class);
    }

    // Method for fetching misc accounts for logging purposes
    public String fetchMiscAccountsForLogging(FetchAccountsRequest request) {
        HttpResponse rawResponse = createRequest(BancoPopularConstants.Urls.FETCH_ACCOUNTS_URL,
                BancoPopularConstants.ApiService.ACCOUNTS_PATH,
                request)
                .post(HttpResponse.class, request);

        handleResponse(rawResponse);

        return rawResponse.getBody(String.class);
    }

    // just logging request/response for the time being
    public String fetchLoanAccounts(FetchAccountsRequest request) {
        return fetchMiscAccountsForLogging(request);
    }

    // just logging request/response for the time being
    public String fetchFundAccounts(FetchAccountsRequest request) {
        return fetchMiscAccountsForLogging(request);
    }

    // just logging request/response for the time being
    public String fetchSecuritiesAccounts(FetchAccountsRequest request) {
        return fetchMiscAccountsForLogging(request);
    }

    // just logging request/response for the time being
    public String fetchCreditAccounts(FetchAccountsRequest request) {
        return fetchMiscAccountsForLogging(request);
    }

    public FetchTransactionsResponse fetchTransactions(
            FetchTransactionsRequest fetchTransactionsRequest) {
        HttpResponse rawResponse =
                createRequest(BancoPopularConstants.Urls.FETCH_TRANSACTIONS_URL,
                BancoPopularConstants.ApiService.TRANSACTIONS_PATH,
                fetchTransactionsRequest)
                .post(HttpResponse.class, fetchTransactionsRequest);

        handleResponse(rawResponse);

        return rawResponse.getBody(FetchTransactionsResponse.class);
    }

    public KeepAliveResponse keepAlive() throws SessionException {
        try {
            HttpResponse rawResponse = createRequest(BancoPopularConstants.Urls.KEEP_ALIVE_URL,
                    BancoPopularConstants.ApiService.KEEP_ALIVE_PATH,
                    Collections.EMPTY_MAP)
                    .post(HttpResponse.class, Collections.EMPTY_MAP);

            handleResponse(rawResponse);

            return rawResponse.getBody(KeepAliveResponse.class);
        } catch (HttpResponseException e) {
            int statusCode = e.getResponse().getStatus();
            if (statusCode == BancoPopularConstants.StatusCodes.SESSION_EXPIRED) {
                LOGGER.trace("No session found");
                throw SessionError.SESSION_EXPIRED.exception();
            }

            LOGGER.warn("Keep session alive failed: " + statusCode);
            throw e;
        }
    }

    private void ifLoginErrorThrowLoginException(int statusCode) throws LoginException {
        if (statusCode == BancoPopularConstants.StatusCodes.INCORRECT_USERNAME_PASSWORD) {
            LOGGER.trace("Login failed, incorrect username/password");
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        if (statusCode == BancoPopularConstants.StatusCodes.INCORRECT_TOKEN) {   // bad TKN_CRC
            setAuthorization("");  // reset Authorization to start with a fresh value
            client.clearPersistentHeaders();

            LOGGER.trace("Login failed, incorrect TKN_CRC");
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    /*
     * Extract and store last authorization from response
     */
    private void handleResponse(HttpResponse rawResponse) {
        String authorization = rawResponse.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        setAuthorization(authorization);
    }

    /*
     * Create a base request with default fields set
     */
    private RequestBuilder createRequest(URL url, String path, Object request) {
        String token = calculateToken(
                path, request);

        return client
                .request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header(BancoPopularConstants.Authentication.HEADER_COD_PLATAFORMA,
                        BancoPopularConstants.Authentication.PLATAFORMA)
                .header(BancoPopularConstants.Authentication.HEADER_TKN_CRC, token)
                .header(HttpHeaders.AUTHORIZATION, getAuthorization());
    }

    private String calculateToken(String servicePath, Object messageObject) {

        try {
            String message = messageToString(messageObject);

            String cryptoKey = calculateCryptoKey(servicePath, getAuthorization());
            byte[] byteKey = cryptoKey.getBytes("UTF-8");

            Mac sha512_HMAC = Mac.getInstance(BancoPopularConstants.Authentication.CRYPTO_ALG);
            SecretKeySpec keySpec = new SecretKeySpec(byteKey,
                    BancoPopularConstants.Authentication.CRYPTO_ALG);
            sha512_HMAC.init(keySpec);
            byte[] macAsBytes = sha512_HMAC.doFinal(message.getBytes());

            return Hex.encodeHexString(macAsBytes);
        } catch (Exception e) {
            LOGGER.warn("Could not create TKN-CRC token");
            throw new IllegalStateException("Could not create TKN-CRC token", e);
        }
    }

    private String messageToString(Object message) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(message);
    }

    private String calculateCryptoKey(String servicePath, String authorization) {

        String cryptoKey = "";
        if (authorization.length() > 10) {
            cryptoKey += authorization.substring(3, 10);
        }
        if (authorization.length() > 22) {
            cryptoKey += authorization.substring(16, 22);
        }
        if (authorization.length() > 55) {
            cryptoKey += authorization.substring(45, 55);
        }

        cryptoKey += servicePath;

        return cryptoKey;
    }

    // retrieve persistent header for authorization
    private String getAuthorization() {
        String authorization = sessionStorage.get(HttpHeaders.AUTHORIZATION);
        return authorization == null ? "" : authorization;
    }

    // set persitent header for authorization
    private void setAuthorization(String authorization) {
        sessionStorage.put(HttpHeaders.AUTHORIZATION, authorization);
    }

    private String getClientIP() {
        String clientIp =
                sessionStorage.get(BancoPopularConstants.Authentication.HEADER_X_CLIENT_IP);
        return clientIp == null ? "" : clientIp;
    }

    private void setClientIP(String clientIp) {
        sessionStorage.put(BancoPopularConstants.Authentication.HEADER_X_CLIENT_IP, clientIp);
    }
}
