package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator;

import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdLoginWithInstallIdResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.rpc.NemIdLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.rpc.NemIdResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Decryptor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Token;

public class JyskeServiceAuthenticator {
    private final JyskeApiClient apiClient;

    public JyskeServiceAuthenticator(JyskeApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void authenticate(NemIdResponse encryption, Token token) throws LoginException {
        NemIdLoginWithInstallIdResponse loginWithInstallId =
                new Decryptor(token).read(encryption, NemIdLoginWithInstallIdResponse.class);
        NemIdLoginResponse mobileServiceInit = apiClient.mobilServiceInit(token);
        if (!mobileServiceInit.isOk()) {
            throw LoginError.ERROR_WITH_MOBILE_OPERATOR.exception();
        }

        NemIdLoginResponse mobilServiceLogin =
                apiClient.mobilServiceLogin(loginWithInstallId, token);
        if (!mobilServiceLogin.isOk()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }
}
