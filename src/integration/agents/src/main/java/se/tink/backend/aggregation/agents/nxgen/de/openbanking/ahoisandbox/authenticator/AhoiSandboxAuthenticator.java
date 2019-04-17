package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.AhoiSandboxApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.AhoiSandboxConstants;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.AhoiSandboxConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.rpc.BankingTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.rpc.ProviderDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.rpc.ProvidersListRsponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.rpc.RegistrationTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.rpc.UserRegistrationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.configuration.AhoiSandboxConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class AhoiSandboxAuthenticator implements PasswordAuthenticator {

    private final AhoiSandboxApiClient apiClient;
    private final AhoiSandboxConfiguration configuration;
    private final PersistentStorage persistentStorage;

    public AhoiSandboxAuthenticator(
        AhoiSandboxApiClient apiClient, AhoiSandboxConfiguration configuration,
        PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.configuration = configuration;
        this.persistentStorage = persistentStorage;
    }

    private AhoiSandboxConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        Credentials credentials = new Credentials();
        credentials.setUsername(username);
        credentials.setPassword(password);

        final RegistrationTokenResponse registrationToken = apiClient.getRegistrationTokenResponse();

        final UserRegistrationResponse userRegistrationResponse =
            apiClient.getUserRegistrationResponse(registrationToken.getAccessToken());

        final BankingTokenResponse bankingTokenResponse =
            apiClient.getBankingTokenResponse(
                userRegistrationResponse.getInstallation()); // Banking token = access token

        persistentStorage.put(
            AhoiSandboxConstants.StorageKeys.ACCESS_TOKEN,
            bankingTokenResponse.getAccessToken());

        final ProvidersListRsponse providersListRsponse = apiClient.getProviderEntities();

        final ProviderDetailsResponse providerDetailsResponse =
            apiClient.getProviderDetailsResponse(providersListRsponse.get(0).getId());

        apiClient.createAccess(credentials, providerDetailsResponse);
    }
}
