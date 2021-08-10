package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.authenticator;

import com.google.common.base.Preconditions;
import java.util.Optional;
import org.apache.http.HttpStatus;
import org.w3c.dom.Node;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants.SoapErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.SoapFaultErrorEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils.SantanderEsXmlUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SantanderEsAuthenticator implements PasswordAuthenticator {

    private final SantanderEsApiClient apiClient;
    private final SantanderEsSessionStorage santanderEsSessionStorage;

    public SantanderEsAuthenticator(
            SantanderEsApiClient apiClient, SantanderEsSessionStorage santanderEsSessionStorage) {
        this.apiClient = apiClient;
        this.santanderEsSessionStorage = santanderEsSessionStorage;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        String responseString = "";
        try {
            responseString = apiClient.authenticateCredentials(username, password);
        } catch (HttpResponseException e) {
            handleHttpResponseException(e);
        }

        // Parse token credential and add it to api client to be used for future requests
        Node tokenCredentialNode =
                SantanderEsXmlUtils.getTagNodeFromSoapString(
                        responseString, SantanderEsConstants.NodeTags.TOKEN_CREDENTIAL);

        String tokenCredential =
                Preconditions.checkNotNull(
                        tokenCredentialNode.getFirstChild().getTextContent(),
                        "Could not parse token credentials.");
        apiClient.setTokenCredential(tokenCredential);
        // store in session storage so that it will be masked in the log
        santanderEsSessionStorage.put(SantanderEsConstants.Storage.ACCESS_TOKEN, tokenCredential);

        try {
            responseString = apiClient.login();
        } catch (HttpResponseException e) {
            if (e.getResponse().getBody(String.class).contains(ErrorCodes.AUTHENTICATION_ERROR)) {
                throw new LoginException(LoginError.DEFAULT_MESSAGE, e);
            }
            handleNotCustomer(e);
            throw e;
        }
        santanderEsSessionStorage.setLoginResponse(responseString);
        santanderEsSessionStorage.setIdNumber(username);
        santanderEsSessionStorage.setPassword(password);
    }

    private void handleHttpResponseException(HttpResponseException e) {
        Node n =
                SantanderEsXmlUtils.getTagNodeFromSoapString(
                        e.getResponse().getBody(String.class),
                        SantanderEsConstants.NodeTags.CODIGO_ERROR);

        Optional.ofNullable(n)
                .map(
                        node -> {
                            String errorCode =
                                    node.getFirstChild().getTextContent().trim().toUpperCase();
                            if (ErrorCodes.INCORRECT_CREDENTIALS.stream()
                                    .anyMatch(code -> code.equalsIgnoreCase(errorCode))) {
                                throw new LoginException(LoginError.INCORRECT_CREDENTIALS, e);

                            } else if (ErrorCodes.BLOCKED_CREDENTIALS.stream()
                                    .anyMatch(code -> code.equalsIgnoreCase(errorCode))) {
                                throw new AuthorizationException(
                                        AuthorizationError.ACCOUNT_BLOCKED, e);
                            } else if (ErrorCodes.AUTHENTICATION_ERROR.equalsIgnoreCase(
                                    errorCode)) {
                                throw new LoginException(LoginError.DEFAULT_MESSAGE, e);
                            } else {
                                throw e;
                            }
                        })
                .orElseThrow(() -> e);
    }

    private void handleNotCustomer(HttpResponseException e) throws LoginException {
        Optional.of(e.getResponse())
                .filter(resp -> resp.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .map(resp -> resp.getBody(String.class))
                .map(SoapFaultErrorEntity::parseFaultErrorFromSoapError)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(fault -> !fault.matchesErrorMessage(SoapErrorMessages.NOT_CUSTOMER))
                .orElseThrow(() -> LoginError.NOT_CUSTOMER.exception(e));
    }
}
