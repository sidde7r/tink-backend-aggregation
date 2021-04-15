package se.tink.backend.aggregation.agents.nxgen.demo.banks.password.authenticator;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;

@Slf4j
public class PasswordAuthenticator implements TypedAuthenticator {

    private static final Map<String, String> TEST_CREDENTIALS =
            new ImmutableMap.Builder<String, String>()
                    .put("tink", "tink-1234")
                    .put("tink2", "tink-2345")
                    .put("tink3", "tink-3456")
                    .put("tink-4", "tink-4567")
                    .put("tink-äöü", "tink-5-password")
                    .put("tink6", "tink-äöü")
                    .put(
                            "tink-!#$&()*+,-./:;=?@[] ^_%§äöüÄÖÜßåæéø",
                            "tink-äöüÄÖ,Üß!$%&/() =?+#,.-:@+!*åæéø")
                    .build();

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        logDetails(credentials);

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        if (!(TEST_CREDENTIALS.containsKey(username)
                && TEST_CREDENTIALS.get(username).equals(password))) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    // For the purpose of making sure that the fields we get through the whole system stay the same
    // till agent level
    private void logDetails(Credentials credentials) {
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        log.info(
                "Credentials Id [{}] fieldsSerializedHash [{}] username [{}] usernameHash [{}] usernameBytes [{}] password [{}] passwordHash [{}] passwordBytes [{}]",
                credentials.getId(),
                getHash(credentials.getFieldsSerialized()),
                username,
                getHash(username),
                toStringOfBytes(username),
                password,
                getHash(password),
                toStringOfBytes(password));
    }

    private int getHash(String message) {
        return message == null ? 0 : message.hashCode();
    }

    private String toStringOfBytes(String message) {
        return message == null ? "" : Arrays.toString(message.getBytes());
    }
}
