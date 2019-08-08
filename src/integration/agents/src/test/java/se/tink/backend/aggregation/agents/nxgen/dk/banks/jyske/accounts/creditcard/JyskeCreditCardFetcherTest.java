package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.creditcard;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeTestConfig;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.JyskeAutoAuthenticator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class JyskeCreditCardFetcherTest {

    @Test
    public void canFetchCards() throws Exception {
        JyskeTestConfig.User user = JyskeTestConfig.USER_2;

        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setField(Field.Key.USERNAME, user.username);
        credentials.setField(Field.Key.PASSWORD, user.mobilCode);
        JyskeApiClient apiClient = new JyskeApiClient(new TinkHttpClient());
        new JyskeAutoAuthenticator(apiClient, user.persistentStorage).autoAuthenticate(credentials);

        JyskeCreditCardFetcher creditCardFetcher = new JyskeCreditCardFetcher(apiClient);

        Collection<CreditCardAccount> creditCardAccounts = creditCardFetcher.fetchAccounts();

        assertNotNull(creditCardAccounts);
        assertTrue(creditCardAccounts.isEmpty());
    }
}
