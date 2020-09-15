package se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.EdenredConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.EdenredConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.authenticator.rpc.AuthenticationPinRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.authenticator.rpc.AuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.authenticator.rpc.SetPinRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.authenticator.rpc.SetPinResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.rpc.CardListResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.storage.EdenredStorage;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class EdenredApiClient {

    private final TinkHttpClient httpClient;

    private final EdenredStorage edenredStorage;

    public AuthenticationResponse authenticateDefault(String username, String password) {
        AuthenticationRequest authenticationRequest =
                AuthenticationRequest.builder()
                        .rememberMe(true)
                        .appType(EdenredConstants.APP_TYPE)
                        .channel(EdenredConstants.CHANNEL)
                        .appVersion(EdenredConstants.APP_VERSION)
                        .userId(username)
                        .password(password)
                        .build();

        try {
            return request(Urls.AUTHENTICATE_DEFAULT)
                    .body(authenticationRequest)
                    .post(AuthenticationResponse.class);
        } catch (HttpResponseException exception) {
            throw mapAuthenticationException(exception);
        }
    }

    public SetPinResponse setupPin(String userId, String pin) {
        SetPinRequest setPinRequest =
                SetPinRequest.builder()
                        .userId(userId)
                        .password(pin)
                        .appType(EdenredConstants.APP_TYPE)
                        .appVersion(EdenredConstants.APP_VERSION)
                        .build();

        try {
            return requestProtected(Urls.SET_PIN).body(setPinRequest).post(SetPinResponse.class);
        } catch (HttpResponseException exception) {
            throw mapException(exception);
        }
    }

    public AuthenticationResponse authenticatePin(String userId, String pin) {
        AuthenticationPinRequest authenticationPinRequest =
                AuthenticationPinRequest.builder()
                        .appType(EdenredConstants.APP_TYPE)
                        .appVersion(EdenredConstants.APP_VERSION)
                        .userId(userId)
                        .password(pin)
                        .build();

        try {
            return request(Urls.AUTHENTICATE_PIN)
                    .body(authenticationPinRequest)
                    .post(AuthenticationResponse.class);
        } catch (HttpResponseException exception) {
            throw mapAuthenticationException(exception);
        }
    }

    public CardListResponse getCards() {
        try {
            return requestProtected(Urls.CARD_LIST).get(CardListResponse.class);
        } catch (HttpResponseException exception) {
            throw mapException(exception);
        }
    }

    public TransactionsResponse getTransactions(long cardId) {
        try {
            return requestProtected(Urls.TRANSACTIONS.parameter("id", String.valueOf(cardId)))
                    .get(TransactionsResponse.class);
        } catch (HttpResponseException exception) {
            throw mapException(exception);
        }
    }

    private RequestBuilder request(URL url) {
        return httpClient
                .request(url)
                .header(Headers.USER_AGENT, Headers.USER_AGENT_VALUE)
                .header(Headers.CONTENT_TYPE, Headers.APPLICATION_JSON);
    }

    private RequestBuilder requestProtected(URL url) {
        return request(url).header(Headers.AUTHORIZATION, edenredStorage.getToken());
    }

    private LoginException mapAuthenticationException(HttpResponseException exception) {
        if (exception.getResponse().getStatus() == 409) {
            return LoginError.INCORRECT_CREDENTIALS.exception();
        } else {
            return LoginError.DEFAULT_MESSAGE.exception(exception);
        }
    }

    private BankServiceException mapException(HttpResponseException exception) {
        return BankServiceError.BANK_SIDE_FAILURE.exception(exception);
    }
}
