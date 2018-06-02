package se.tink.backend.main.auth;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import se.tink.backend.common.providers.MarketProvider;
import se.tink.backend.core.Client;
import se.tink.backend.main.auth.exceptions.UnsupportedClientException;
import se.tink.backend.main.auth.validators.ClientValidator;
import se.tink.backend.main.providers.ClientProvider;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientValidatorTest {

    private ClientProvider clientProvider;
    @Mock MarketProvider marketProvider;

    private static Client client(String id, boolean allowed) {
        Client c = new Client();
        c.setId(id);
        c.setAllowed(allowed);
        return c;
    }

    @Before
    public void setUp() {
        clientProvider = mock(ClientProvider.class);

        when(clientProvider.get()).thenReturn(ImmutableMap.of(
                "clientKey1", client("id1", false),
                "clientKey2", client("id2", true)
        ));

    }

    @Test
    public void verifyNullClientKeyReturnsAbsentClientButDoesNotThrow() {
        ClientValidator validator = new ClientValidator(clientProvider, marketProvider);

        Optional<Client> client = validator.validateClient(null, "nl_NL");

        Assert.assertFalse(client.isPresent());
    }

    @Test(expected = UnsupportedClientException.class)
    public void verifyNonExistingClientKeyThrowsException() {
        ClientValidator validator = new ClientValidator(clientProvider, marketProvider);
        validator.validateClient("nonExistingKey", "nl_NL");
    }

    @Test(expected = UnsupportedClientException.class)
    public void verifyExistingDisallowedClientKeyThrowsException() {
        ClientValidator validator = new ClientValidator(clientProvider, marketProvider);

        validator.validateClient("clientKey1", "nl_NL");
    }

    @Test
    public void verifyExistingAllowedClientKeyReturnsCorrectClient() {
        ClientValidator validator = new ClientValidator(clientProvider, marketProvider);

        Optional<Client> client = validator.validateClient("clientKey2", "nl_NL");

        Assert.assertTrue(client.isPresent());
        Assert.assertEquals("id2", client.get().getId());
    }

}
