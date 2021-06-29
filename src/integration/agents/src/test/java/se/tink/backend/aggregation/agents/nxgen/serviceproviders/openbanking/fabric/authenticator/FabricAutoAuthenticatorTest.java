package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.StorageKeys;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class FabricAutoAuthenticatorTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/fabric/authenticator/resources/";

    private static final String CONSENT_ID = "consentId";

    private FabricAutoAuthenticator fabricAutoAuthenticator;
    private FabricApiClient fabricApiClient;
    private PersistentStorage persistentStorage;

    @Before
    public void setup() {
        persistentStorage = mock(PersistentStorage.class);
        fabricApiClient = mock(FabricApiClient.class);

        fabricAutoAuthenticator = new FabricAutoAuthenticator(persistentStorage, fabricApiClient);
    }

    @Test
    public void autoAuthenticateShouldPassIfConsentIsValid() {
        // given
        when(persistentStorage.get(StorageKeys.CONSENT_ID, String.class))
                .thenReturn(Optional.of(CONSENT_ID));
        when(fabricApiClient.getConsentStatus(CONSENT_ID))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "fabricValidConsentStatusResponse.json")
                                        .toFile(),
                                ConsentStatusResponse.class));

        // then
        assertThatCode(() -> fabricAutoAuthenticator.autoAuthenticate()).doesNotThrowAnyException();
    }

    @Test
    public void autoAuthenticateShouldThrowExceptionIfConsentIsNotPresent() {
        // given
        when(persistentStorage.get(StorageKeys.CONSENT_ID, String.class))
                .thenReturn(Optional.empty());

        // when
        Throwable t = catchThrowable(() -> fabricAutoAuthenticator.autoAuthenticate());

        // then
        assertThat(t)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_EXPIRED");
    }

    @Test
    public void autoAuthenticateShouldThrowExceptionIfConsentIsInvalid() {
        // given
        when(persistentStorage.get(StorageKeys.CONSENT_ID, String.class))
                .thenReturn(Optional.of(CONSENT_ID));
        when(fabricApiClient.getConsentStatus(CONSENT_ID))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "fabricInvalidConsentStatusResponse.json")
                                        .toFile(),
                                ConsentStatusResponse.class));

        // when
        Throwable t = catchThrowable(() -> fabricAutoAuthenticator.autoAuthenticate());

        // then
        assertThat(t)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_EXPIRED");
    }
}
