package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.password.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankConstants.Payload;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankCryptoUtil;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.password.authenticator.entity.EncryptionValuesEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.password.authenticator.entity.TokenEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class ErsteBankPasswordAuthenticator implements PasswordAuthenticator {

    private final ErsteBankApiClient ersteBankApiClient;
    private final Credentials credentials;

    public ErsteBankPasswordAuthenticator(
            Credentials credentials, ErsteBankApiClient ersteBankApiClient) {
        this.credentials = credentials;
        this.ersteBankApiClient = ersteBankApiClient;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        final EncryptionValuesEntity encryptionValuesEntity =
                ersteBankApiClient.getEncryptionValues(username);
        final String rsa = getRsa(encryptionValuesEntity, password);
        credentials.setSensitivePayload(Payload.RSA, rsa);
        final HttpResponse response = ersteBankApiClient.sendPassword(rsa);

        if (containsToken(response)) {
            final TokenEntity tokenEntity = ErsteBankCryptoUtil.getTokenFromResponse(response);
            ersteBankApiClient.saveToken(tokenEntity);
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
            throw new IllegalStateException("Encryption error: " + e.toString(), e);
        }
    }

    private boolean containsToken(HttpResponse response) {
        return response.getStatus() == 302
                && response.getHeaders().containsKey(ErsteBankConstants.LOCATION);
    }
}
