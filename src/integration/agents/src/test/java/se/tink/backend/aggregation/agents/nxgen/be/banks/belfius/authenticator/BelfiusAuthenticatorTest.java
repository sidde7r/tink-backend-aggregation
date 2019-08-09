package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants.MultiFactorAuthentication.CODE;
import static se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants.Storage.DEVICE_TOKEN;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusTest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.TestConfig;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BelfiusAuthenticatorTest extends BelfiusTest {

    @Test
    public void canMultiFactorAuthenticate() throws Exception {
        String userCode = "Intercept me twice!";

        when(this.supplementalInformation.askSupplementalInformation(any()))
                .thenReturn(ImmutableMap.of(CODE, userCode));
        PersistentStorage persistentStorage = new PersistentStorage();

        BelfiusAuthenticator authenticator = setupAuthentication(persistentStorage, null);

        authenticator.authenticate(TestConfig.USERNAME, TestConfig.PASSWORD);

        assertFalse(persistentStorage.isEmpty());
        assertTrue(persistentStorage.get(DEVICE_TOKEN, String.class).isPresent());
    }

    @Test
    public void canAutoAuthenticate() throws Exception {
        autoAuthenticate();
        verify(this.apiClient, times(1)).login(anyNoneBlank(), anyNoneBlank(), anyNoneBlank());
    }

    @Test
    public void transferTest() throws Exception {
        autoAuthenticate();
        verify(this.apiClient, times(1)).login(anyNoneBlank(), anyNoneBlank(), anyNoneBlank());
    }

    @Test
    public void canLoginAuthenticate() throws Exception {
        BelfiusAuthenticator authenticator =
                setupAuthentication(TestConfig.PERSISTENT_STORAGE, TestConfig.CREDENTIALS);

        authenticator.authenticate(TestConfig.USERNAME, TestConfig.PASSWORD);

        verify(this.apiClient, times(1)).login(anyNoneBlank(), anyNoneBlank(), anyNoneBlank());
    }

    private static String anyNoneBlank() {
        return argThat(StringUtils::isNoneBlank);
    }
}
