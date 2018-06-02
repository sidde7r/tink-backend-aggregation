package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.creditcard;

import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeTestConfig;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.JyskeAutoAuthenticator;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JyskeCreditCardFetcherTest {

    @Test
    public void canFetchCards() throws Exception {
        JyskeTestConfig.User user = JyskeTestConfig.USER_2;

        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setField(Field.Key.USERNAME, user.username);
        credentials.setField(Field.Key.PASSWORD, user.mobilCode);
        JyskeApiClient apiClient = new JyskeApiClient(new TinkHttpClient(null, credentials));
        new JyskeAutoAuthenticator(apiClient, user.persistentStorage, credentials).autoAuthenticate();

        JyskeCreditCardFetcher creditCardFetcher = new JyskeCreditCardFetcher(apiClient);

        Collection<CreditCardAccount> creditCardAccounts = creditCardFetcher.fetchAccounts();

        assertNotNull(creditCardAccounts);
        assertTrue(creditCardAccounts.isEmpty());
    }
}
