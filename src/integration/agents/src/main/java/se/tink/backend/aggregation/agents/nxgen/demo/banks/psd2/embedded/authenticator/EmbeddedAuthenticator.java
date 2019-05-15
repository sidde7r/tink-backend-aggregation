package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.embedded.authenticator;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;

public class EmbeddedAuthenticator implements MultiFactorAuthenticator {

    private static final Map<String, String> TEST_CREDENTIALS =
            ImmutableMap.of(
                    "tink", "tink-1234",
                    "tink2", "tink-2345",
                    "tink3", "tink-3456");

    private final int daysUntilSessionExpires;

    public EmbeddedAuthenticator(int daysUntilSessionExpires) {
        this.daysUntilSessionExpires = daysUntilSessionExpires;
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        if (!(TEST_CREDENTIALS.containsKey(username)
                && TEST_CREDENTIALS.get(username).equals(password))) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        Date sessionExpiryDate = Date.from(
                LocalDateTime.now()
                        .plusDays(daysUntilSessionExpires)
                        .atZone(ZoneId.systemDefault())
                        .toInstant());

        credentials.setSessionExpiryDate(sessionExpiryDate);
    }
}
