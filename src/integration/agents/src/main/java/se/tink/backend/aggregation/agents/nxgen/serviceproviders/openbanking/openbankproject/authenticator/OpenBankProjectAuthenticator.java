package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.OpenBankProjectApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.OpenBankProjectConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.authenticator.rpc.DirectLoginTokenResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class OpenBankProjectAuthenticator implements PasswordAuthenticator {
    private final OpenBankProjectApiClient apiClient;
    private final SessionStorage sessionStorage;

    public OpenBankProjectAuthenticator(
            OpenBankProjectApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(String username, String password) {
        DirectLoginTokenResponse tokenResponse = apiClient.getToken(username, password);
        sessionStorage.put(OpenBankProjectConstants.HeaderKeys.TOKEN, tokenResponse.getToken());
    }
}
