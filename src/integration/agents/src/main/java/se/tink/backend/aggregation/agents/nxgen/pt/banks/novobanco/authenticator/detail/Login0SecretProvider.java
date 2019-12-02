package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.detail;

import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.SecurityConfig.SECRET_FORMAT;

import java.security.SecureRandom;
import java.util.Locale;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.SecretKeys;

public class Login0SecretProvider {

    public String getSecret(String pin) {
        return new RSAEncryptor()
                .encrypt(
                        getDataToEncrypt(pin, SecretKeys.INSTANCE_KEY),
                        new RSAPublicServerKey().getKey());
    }

    private String getTimestamp() {
        return TimeStampProvider.getTimestamp();
    }

    private int generateSecureRandomInt() {
        return new SecureRandom().nextInt();
    }

    private String getDataToEncrypt(final String pin, final String instanceKey) {
        return String.format(
                Locale.US,
                SECRET_FORMAT,
                pin,
                getTimestamp(),
                generateSecureRandomInt(),
                instanceKey);
    }
}
