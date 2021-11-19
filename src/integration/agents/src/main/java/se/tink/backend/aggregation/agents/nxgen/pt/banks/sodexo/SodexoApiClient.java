package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.SodexoConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.SodexoConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.SodexoConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc.AuthenticationPinRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc.AuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc.PreloginStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc.SetupPinRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc.SetupPinResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Slf4j
@RequiredArgsConstructor
public class SodexoApiClient {

    private final TinkHttpClient tinkHttpClient;

    private final SodexoStorage sodexoStorage;

    public boolean checkPreloginStatus() {
        try {
            requestWithUserToken(Urls.PRELOGIN_STATUS).get(PreloginStatusResponse.class);
            return true;
        } catch (HttpResponseException e) {
            return false;
        }
    }

    public AuthenticationResponse authenticateWithCredentials(String nif, String password) {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(nif, password);

        try {
            return request(Urls.SING_IN_REGISTER)
                    .header(Headers.CONTENT_TYPE, Headers.CONTENT_TYPE_X_FORM)
                    .post(AuthenticationResponse.class, authenticationRequest);
        } catch (HttpResponseException e) {
            throw mapAuthenticationException(e);
        }
    }

    public SetupPinResponse setupNewPin(String pin) {
        SetupPinRequest setupPinRequest = new SetupPinRequest(pin);

        return requestWithSessionToken(Urls.RESET_PIN)
                .header(Headers.CONTENT_TYPE, Headers.CONTENT_TYPE_X_FORM)
                .post(SetupPinResponse.class, setupPinRequest);
    }

    public AuthenticationResponse authenticateWithPin(String pin) {
        AuthenticationPinRequest authenticationPinRequest = new AuthenticationPinRequest(pin);
        try {
            return requestWithUserToken(Urls.SING_IN)
                    .header(Headers.CONTENT_TYPE, Headers.CONTENT_TYPE_X_FORM)
                    .post(AuthenticationResponse.class, authenticationPinRequest);
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            SetupPinResponse responseBody = response.getBody(SetupPinResponse.class);
            if (response.getStatus() == 403
                    && ErrorMessages.AUTH_ERROR.equals(responseBody.getMessage())) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
            throw e;
        }
    }

    public BalanceResponse getBalanceResponse() {
        return requestWithSessionToken(Urls.BALANCE).post(BalanceResponse.class);
    }

    public TransactionResponse getTransactions() {
        return requestWithSessionToken(Urls.GET_TRANSACTION).post(TransactionResponse.class);
    }

    private RequestBuilder request(URL url) {
        return tinkHttpClient
                .request(url)
                .header(Headers.X_APP_VERSION, Headers.X_APP_VERSION_VALUE);
    }

    private RequestBuilder requestWithSessionToken(URL url) {
        return request(url)
                .header(Headers.AUTHORIZATION, "Bearer " + sodexoStorage.getSessionToken());
    }

    private RequestBuilder requestWithUserToken(URL url) {
        return request(url).header(Headers.AUTHORIZATION, "Bearer " + sodexoStorage.getUserToken());
    }

    private LoginException mapAuthenticationException(HttpResponseException exception) {
        if (isIncorrectCredentialsResponseStatus(exception)) {
            HttpResponse response = exception.getResponse();
            return LoginError.INCORRECT_CREDENTIALS.exception(
                    String.format(
                            "Cause: LoginError.INCORRECT_CREDENTIALS. Http status: %s. Error body: %s",
                            response.getStatus(), response.getBody(String.class)));
        } else {
            throw exception;
        }
    }

    private boolean isIncorrectCredentialsResponseStatus(HttpResponseException exception) {
        int status = exception.getResponse().getStatus();
        return status == 409 || status == 400 || status == 403;
    }
}
