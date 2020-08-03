package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.rpc.GenerateMatrixRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.rpc.GenerateMatrixResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.identity.rpc.IdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.session.rpc.LogoutResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.storage.BoursoramaPersistentStorage;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BoursoramaApiClient {
    private final TinkHttpClient client;
    private final BoursoramaPersistentStorage boursoramaPersistentStorage;
    private final SessionStorage sessionStorage;

    // The only two headers I haven't added (because the agent works flawlessly without them):
    // _Note:_ User-Agent has the udid baked into it
    //
    // User-Agent: Brs-application_fr_ios_v6/6.4.6-build-364 (UDID
    // 73E434AB-876E-5D26-8C1C-3081B4F67204; Device Apple iPhone X; Platform iOS 13.3.1) Mozilla/5.0
    // (iPhone; CPU iPhone OS 13_3_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko)
    // Mobile/15E148
    //
    // X-Referer-Feature-Id: customer.login.application_fr_ios_v6
    public BoursoramaApiClient(
            TinkHttpClient client,
            BoursoramaPersistentStorage boursoramaPersistentStorage,
            SessionStorage sessionStorage) {
        this.client = client;
        this.boursoramaPersistentStorage = boursoramaPersistentStorage;
        this.sessionStorage = sessionStorage;
    }

    public GenerateMatrixResponse generateMatrix() {
        GenerateMatrixRequest generateMatrixRequest =
                GenerateMatrixRequest.create(
                        BoursoramaConstants.Numpad.IMAGE_SET_VERSION,
                        BoursoramaConstants.Numpad.NUMPAD_MAX_NUMBER_OF_BUTTONS);

        return client.request(BoursoramaConstants.Urls.NUMPAD)
                .header(
                        BoursoramaConstants.Auth.AUTHORIZATION_HEADER,
                        "Bearer " + BoursoramaConstants.Auth.API_KEY)
                .body(generateMatrixRequest, MediaType.APPLICATION_JSON_TYPE)
                .post(GenerateMatrixResponse.class);
    }

    public void login(LoginRequest loginRequest) throws LoginException {
        HttpResponse response;
        try {
            response =
                    client.request(BoursoramaConstants.Urls.LOGIN)
                            .header(
                                    BoursoramaConstants.Auth.AUTHORIZATION_HEADER,
                                    "Bearer " + BoursoramaConstants.Auth.API_KEY)
                            .header(
                                    BoursoramaConstants.Auth.X_SESSION_ID_HEADER,
                                    boursoramaPersistentStorage.getUdid())
                            .body(loginRequest, MediaType.APPLICATION_JSON_TYPE)
                            .post(HttpResponse.class);
        } catch (HttpResponseException hre) {
            ErrorResponse error = hre.getResponse().getBody(ErrorResponse.class);
            if (error.getError().getCode()
                    == BoursoramaConstants.Errors.INVALID_USERNAME_OR_PASSWORD) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
            throw hre;
        }

        MultivaluedMap<String, String> headers = response.getHeaders();
        if (!headers.containsKey(BoursoramaConstants.Auth.USER_HASH_HEADER)) {
            throw new IllegalArgumentException(
                    "Missing user hash from login request, unable to continue.");
        }
        if (!headers.containsKey(BoursoramaConstants.Auth.AUTHORIZATION_HEADER)) {
            throw new IllegalArgumentException("Missing new Bearer token, unable to continue.");
        }
        String newBearerToken = headers.getFirst(BoursoramaConstants.Auth.AUTHORIZATION_HEADER);
        sessionStorage.put(BoursoramaConstants.Storage.LOGGED_IN_BEARER_TOKEN, newBearerToken);

        String userHash = headers.getFirst(BoursoramaConstants.Auth.USER_HASH_HEADER);
        boursoramaPersistentStorage.saveUserHash(userHash);

        LoginResponse loginResponse = response.getBody(LoginResponse.class);
        boursoramaPersistentStorage.saveDeviceEnrolmentTokenValue(
                BoursoramaConstants.Auth.DEVICE_ENROLMENT_TOKEN_VALUE_PREFIX
                        + loginResponse.getDeviceEnrolmentTokenValue());
    }

    public ListAccountsResponse getAccounts() {
        String loggedInBearerToken =
                sessionStorage.get(BoursoramaConstants.Storage.LOGGED_IN_BEARER_TOKEN);
        return client.request(createUserHashUrl(BoursoramaConstants.UserUrls.LIST_ACCOUNTS))
                .header(BoursoramaConstants.Auth.AUTHORIZATION_HEADER, loggedInBearerToken)
                .header(
                        BoursoramaConstants.Auth.X_SESSION_ID_HEADER,
                        boursoramaPersistentStorage.getUdid())
                .get(ListAccountsResponse.class);
    }

    public IdentityResponse getIdentityData() {
        String loggedInBearerToken =
                sessionStorage.get(BoursoramaConstants.Storage.LOGGED_IN_BEARER_TOKEN);
        return client.request(createUserHashUrl(BoursoramaConstants.UserUrls.IDENTITY_DATA))
                .header(BoursoramaConstants.Auth.AUTHORIZATION_HEADER, loggedInBearerToken)
                .header(
                        BoursoramaConstants.Auth.X_SESSION_ID_HEADER,
                        boursoramaPersistentStorage.getUdid())
                .get(IdentityResponse.class);
    }

    public TransactionsResponse getTransactions(String accountKey, String continuationToken) {
        String loggedInBearerToken =
                sessionStorage.get(BoursoramaConstants.Storage.LOGGED_IN_BEARER_TOKEN);
        RequestBuilder requestBuilder =
                client.request(
                                createUserHashUrl(
                                        BoursoramaConstants.UserUrls.LIST_TRANSACTIONS_FROM_ACCOUNT
                                                + accountKey))
                        .header(BoursoramaConstants.Auth.AUTHORIZATION_HEADER, loggedInBearerToken)
                        .header(
                                BoursoramaConstants.Auth.X_SESSION_ID_HEADER,
                                boursoramaPersistentStorage.getUdid());
        if (StringUtils.isNotEmpty(continuationToken)) {
            requestBuilder.queryParam(
                    BoursoramaConstants.Transaction.CONTINUATION_TOKEN_QUERY_KEY,
                    continuationToken);
        }

        return requestBuilder.get(TransactionsResponse.class);
    }

    public void logout() {
        String loggedInBearerToken =
                sessionStorage.get(BoursoramaConstants.Storage.LOGGED_IN_BEARER_TOKEN);
        LogoutResponse logoutResponse =
                client.request(createUserHashUrl(BoursoramaConstants.UserUrls.LOGOUT))
                        .header(BoursoramaConstants.Auth.AUTHORIZATION_HEADER, loggedInBearerToken)
                        .header(
                                BoursoramaConstants.Auth.X_SESSION_ID_HEADER,
                                boursoramaPersistentStorage.getUdid())
                        .post(LogoutResponse.class);

        if (!logoutResponse.isSuccess()) {
            throw new IllegalArgumentException("Unable to logout!");
        }
    }

    public boolean isAlive() {
        String userHash = boursoramaPersistentStorage.getUserHash();
        if (Strings.isNullOrEmpty(userHash)) {
            return false;
        }

        String loggedInBearerToken =
                sessionStorage.get(BoursoramaConstants.Storage.LOGGED_IN_BEARER_TOKEN);
        try {
            client.request(createUserHashUrl(BoursoramaConstants.UserUrls.KEEP_ALIVE))
                    .header(BoursoramaConstants.Auth.AUTHORIZATION_HEADER, loggedInBearerToken)
                    .header(
                            BoursoramaConstants.Auth.X_SESSION_ID_HEADER,
                            boursoramaPersistentStorage.getUdid())
                    .post(HttpResponse.class);
        } catch (HttpResponseException hre) {
            // 401: {"code":401,"message":"JWT Token not found"}
            // But we can consider all error responses as not alive.
            return false;
        }
        return true;
    }

    private URL createUserHashUrl(String urlBase) {
        String userHash = boursoramaPersistentStorage.getUserHash();
        return new URL(String.format(urlBase, userHash));
    }
}
