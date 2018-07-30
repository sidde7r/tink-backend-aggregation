package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken;

import java.util.HashMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities.Link;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.AuthorizeResponse;

public class HandelsbankenSessionHandlerTest extends HandelsbankenSEAuthenticatedTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void canHandleExpiredSession() throws SessionException {
        exception.expect(SessionException.class);

        AuthorizeResponse authorize = new AuthorizeResponse();
        authorize.setLinks(createLink());
        persistentStorage.persist(authorize);

        new HandelsbankenSessionHandler(client, persistentStorage, credentials, sessionStorage).keepAlive();
    }

    @Test
    public void sessionIsProlonged() throws SessionException {
        autoAuthenticator.autoAuthenticate();

        new HandelsbankenSessionHandler(client, persistentStorage, credentials, sessionStorage).keepAlive();
    }

    private static Map<String, Link> createLink() {
        Map<String, Link> links = new HashMap<>();
        links.put(HandelsbankenConstants.URLS.Links.APPLICATION_ENTRY_POINT.getName(),
                new Link().setHref("https://m2.handelsbanken.se/fipriv/session"));
        return links;
    }
}
