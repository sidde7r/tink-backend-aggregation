package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken;

import com.google.common.collect.Lists;
import java.util.List;
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

    private static List<Link> createLink() {
        return Lists.newArrayList(new Link()
                .setHref("https://m2.handelsbanken.se/fipriv/session")
                .setRel(HandelsbankenConstants.URLS.Links.APPLICATION_ENTRY_POINT.getName())
        );
    }
}
