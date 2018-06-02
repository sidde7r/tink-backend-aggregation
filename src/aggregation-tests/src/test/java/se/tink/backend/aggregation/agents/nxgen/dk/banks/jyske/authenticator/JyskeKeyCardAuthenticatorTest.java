package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskePersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeTestConfig;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardInitValues;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import static java.util.Optional.empty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class JyskeKeyCardAuthenticatorTest {

    @Test
    public void canAuthenticate() throws Exception {
        JyskeTestConfig.User user = JyskeTestConfig.USER_1;

        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setField(Field.Key.USERNAME, user.username);
        credentials.setField(Field.Key.PASSWORD, user.mobilCode);

        JyskePersistentStorage persistentStorage = new JyskePersistentStorage(new PersistentStorage());
        JyskeKeyCardAuthenticator keyCardAuthenticator = new JyskeKeyCardAuthenticator(
                new JyskeApiClient(new TinkHttpClient(null, credentials)), persistentStorage);

        KeyCardInitValues cardValues = keyCardAuthenticator.init(user.username, user.mobilCode);

        assertNotNull(cardValues);
        assertThat(cardValues.getCardId(), not(empty()));
        assertFalse(isBlank(cardValues.getCardIndex()));

        String code = "Interception required! (Use debug)";
        keyCardAuthenticator.authenticate(code);

        String actualInstallId = persistentStorage.getInstallId();
        assertNotNull(actualInstallId);
        assertFalse(isBlank(actualInstallId));
    }
}
