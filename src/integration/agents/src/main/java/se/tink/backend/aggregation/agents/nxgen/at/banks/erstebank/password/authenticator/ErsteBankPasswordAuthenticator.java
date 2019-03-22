package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.password.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankCryptoUtil;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.password.authenticator.entity.EncryptionValuesEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.password.authenticator.entity.TokenEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;

public class ErsteBankPasswordAuthenticator implements PasswordAuthenticator {

    private final ErsteBankApiClient ersteBankApiClient;

    public ErsteBankPasswordAuthenticator(ErsteBankApiClient ersteBankApiClient) {
        this.ersteBankApiClient = ersteBankApiClient;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        EncryptionValuesEntity encryptionValuesEntity =
                ersteBankApiClient.getEncryptionValues(username);
        String rsa = getRsa(encryptionValuesEntity, password);
        HttpResponse response = ersteBankApiClient.sendPassword(rsa);

        if (containsToken(response)) {
            TokenEntity tokenEntity = ErsteBankCryptoUtil.getTokenFromResponse(response);
            ersteBankApiClient.saveToken(tokenEntity);
            return;
        } else {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    private String getRsa(EncryptionValuesEntity encryptionValuesEntity, String password) {
        try {
            return ErsteBankCryptoUtil.getRSAPassword(
                    encryptionValuesEntity.getSalt(),
                    encryptionValuesEntity.getExponent(),
                    encryptionValuesEntity.getModulus(),
                    password);
        } catch (Exception e) {
            throw new IllegalStateException("Encryption error: " + e.toString());
        }
    }

    private boolean containsToken(HttpResponse response) {
        return response.getStatus() == 302
                && response.getHeaders().containsKey(ErsteBankConstants.LOCATION);
    }
}
