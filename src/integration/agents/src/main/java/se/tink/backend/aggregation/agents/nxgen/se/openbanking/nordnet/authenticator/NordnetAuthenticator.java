package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.NordnetApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.NordnetConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.authenticator.rpc.GetSessionKeyResponse;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;


public class NordnetAuthenticator implements PasswordAuthenticator {
    private final NordnetApiClient apiClient;
    private final SessionStorage sessionStorage;

    public NordnetAuthenticator(NordnetApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(String username, String password) throws AuthenticationException, AuthorizationException {
        String login = toBase64String(username.getBytes())
                + ":"
                + toBase64String(password.getBytes())
                + ":"
                + toBase64String(String.valueOf(System.currentTimeMillis()).getBytes());

        byte[] binary = DatatypeConverter.parseBase64Binary(NordnetConstants.Keys.PUBLIC_KEY);
        RSAPublicKey publicKey2 = RSA.getPubKeyFromBytes(binary);
        byte[] encryptedBytes = RSA.encryptEcbPkcs1(publicKey2, login.getBytes(StandardCharsets.UTF_8));
        String authParam = toBase64String(encryptedBytes);

        GetSessionKeyResponse sessionKeyResponse = apiClient.getSessionKey(authParam);
        sessionStorage.put(NordnetConstants.StorageKeys.SESSION_KEY, sessionKeyResponse.getSessionKey());
    }

    public String toBase64String(byte[] bytes) {
        return DatatypeConverter.printBase64Binary(bytes);
    }
}
