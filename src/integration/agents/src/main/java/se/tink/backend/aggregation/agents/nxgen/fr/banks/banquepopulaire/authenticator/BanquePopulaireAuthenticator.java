package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.entities.ValidationUnit;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.rpc.InitiateSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.rpc.PasswordValidationRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.rpc.PasswordValidationResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.rpc.TokensResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BanquePopulaireAuthenticator implements PasswordAuthenticator {

    private final BanquePopulaireApiClient apiClient;
    private final SessionStorage sessionStorage;

    public BanquePopulaireAuthenticator(
            BanquePopulaireApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        apiClient.getConfiguration();

        HttpResponse rawInitiateResponse = apiClient.initiateSession();
        List<URI> redirects = rawInitiateResponse.getRedirects();
        String baseAuthUrl = redirects.get(redirects.size() - 1).toString();

        InitiateSessionResponse initiateSessionResponse =
                rawInitiateResponse.getBody(InitiateSessionResponse.class);
        HashMap<String, List<ValidationUnit>> validationUnit =
                initiateSessionResponse.getFirstValidationUnit();

        Map.Entry<String, List<ValidationUnit>> unit =
                validationUnit.entrySet().stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No validation unit found"));

        String validationId = unit.getKey();
        ValidationUnit validationData =
                unit.getValue().stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No validation data found"));

        PasswordValidationRequest passwordValidationRequest =
                PasswordValidationRequest.create(
                        validationId,
                        password,
                        validationData.getId(),
                        username,
                        validationData.getType());

        PasswordValidationResponse validationResponse =
                apiClient.authenticate(baseAuthUrl, passwordValidationRequest);
        if (!BanquePopulaireConstants.Authentication.AUTHENTICATION_SUCCESS.equalsIgnoreCase(
                validationResponse.getValidationStatus())) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        try {
            apiClient.authenticateSaml2(validationResponse);
        } catch (HttpResponseException hre) {
            HttpResponse response = hre.getResponse();
            if (response != null
                    && (response.getStatus() == HttpStatus.SC_UNAUTHORIZED
                            || response.getStatus() == HttpStatus.SC_FORBIDDEN)) {
                throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception();
            }
            // if not auth error throw
            throw hre;
        }

        TokensResponse tokensResponse = apiClient.getTokens();
        sessionStorage.put(BanquePopulaireConstants.Storage.TOKENS, tokensResponse);
    }
}
