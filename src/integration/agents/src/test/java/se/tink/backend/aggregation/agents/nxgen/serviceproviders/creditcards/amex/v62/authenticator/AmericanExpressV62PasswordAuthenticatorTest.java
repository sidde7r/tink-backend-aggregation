package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc.LogonResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class AmericanExpressV62PasswordAuthenticatorTest {

    SessionStorage sessionStorage;
    PersistentStorage persistentStorage;
    AmericanExpressV62ApiClient amexClient;
    AmericanExpressV62PasswordAuthenticator authenticator;
    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
        sessionStorage = Mockito.mock(SessionStorage.class);
        persistentStorage = Mockito.mock(PersistentStorage.class);
        amexClient = Mockito.mock(AmericanExpressV62ApiClient.class);
        authenticator =
                new AmericanExpressV62PasswordAuthenticator(
                        amexClient, persistentStorage, sessionStorage);
    }

    @Test
    public void excludeAccountsWithError()
            throws IOException, AuthenticationException, AuthorizationException {
        LogonResponse logonResponse =
                mapper.readValue(
                        AmericanExpressV62PasswordAuthenticatorTestData.logonResponse,
                        LogonResponse.class);

        Mockito.when(amexClient.logon(any())).thenReturn(logonResponse);

        ArgumentCaptor<List<CardEntity>> valueCapture = ArgumentCaptor.forClass(ArrayList.class);

        Mockito.doNothing().when(sessionStorage).put(any(String.class), valueCapture.capture());

        authenticator.authenticate("username", "password");
        List<CardEntity> allValues = valueCapture.getValue();
        assertEquals(1, allValues.size());
    }
}
