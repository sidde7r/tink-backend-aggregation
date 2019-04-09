package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants.URLS.Links.APPLICATION_ENTRY_POINT;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.StorageTestHelper.createLinks;

import java.util.Collections;
import java.util.Optional;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.encryption.LibTFA;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.AuthorizeResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class HandelsbankenPersistentStorageTest {

    @Rule public ExpectedException exception = ExpectedException.none();

    private HandelsbankenPersistentStorage persistentStorage;
    private PersistentStorage persistentMap;
    private Credentials credentials;

    @Before
    public void setUp() throws Exception {
        persistentMap = new PersistentStorage();
        persistentStorage =
                new HandelsbankenPersistentStorage(persistentMap, Collections.emptyMap());
        credentials = new Credentials();
    }

    @Test
    public void canPersistAndLoadTfa() throws Exception {
        LibTFA originalTfa = new LibTFA();
        persistentStorage.persist(originalTfa);

        LibTFA loadedTfa = persistentStorage.getTfa(credentials);

        assertThat(loadedTfa, is(originalTfa));
    }

    @Test
    public void canPersistAndLoadTfaWithMissingDeviceSecurityContextId() throws Exception {
        LibTFA originalTfa = new LibTFA();
        persistentStorage.persist(originalTfa);
        persistentMap.put(HandelsbankenConstants.Storage.DEVICE_SECURITY_CONTEXT_ID, "");

        LibTFA loadedTfa = persistentStorage.getTfa(credentials);

        assertThat(
                "Device Security context id is expected to be different.",
                loadedTfa,
                not(is(originalTfa)));
        String deviceSecurityContextId =
                persistentMap.get(HandelsbankenConstants.Storage.DEVICE_SECURITY_CONTEXT_ID);
        assertThat(
                "A new device security context id was not persisted",
                deviceSecurityContextId,
                not(is("")));
        assertThat(
                "The persisted device security context id is not used by the loaded TFA",
                deviceSecurityContextId,
                is(loadedTfa.getDeviceSecurityContextId()));
        assertThat(
                "The persisted RSA Key has not been used.",
                originalTfa.getDeviceRsaPrivateKey(),
                is(loadedTfa.getDeviceRsaPrivateKey()));
    }

    @Test
    public void cannotLoadTfaWithoutPersistingFirst() throws Exception {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("User has no persisted TFA state, therefore cannot load.");

        persistentStorage.getTfa(credentials);
    }

    @Test
    public void canPersistAuthorizeResponse() throws Exception {
        AuthorizeResponse authorizeResponse = createAuthorizeResponse();

        persistentStorage.persist(authorizeResponse);

        assertThat(persistentStorage.getAuthorizeResponse(), is(Optional.of(authorizeResponse)));
    }

    @Test
    public void removesPersistedAuthorizeResponse() {
        AuthorizeResponse authorizeResponse = createAuthorizeResponse();

        persistentStorage.persist(authorizeResponse);
        persistentStorage.removeAuthorizeResponse();

        assertThat(persistentStorage.getAuthorizeResponse(), is(Optional.empty()));
    }

    private static AuthorizeResponse createAuthorizeResponse() {
        AuthorizeResponse authorizeResponse = new AuthorizeResponse();
        authorizeResponse.setLinks(
                createLinks(APPLICATION_ENTRY_POINT, "https:://authorized.url.com"));
        return authorizeResponse;
    }
}
