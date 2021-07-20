package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SpardaStorageTest {

    @Test
    public void shouldHoldAllValuesCorrectly() {
        // given
        SpardaStorage storage = new SpardaStorage(new PersistentStorage(), new SessionStorage());
        String inputConsentId = "test_consent_id";
        String inputCodeVerifier = "test_code_verifier";
        OAuth2Token inputToken = OAuth2Token.createBearer("test_access", "test_refresh", 1234);

        // when
        storage.saveConsentId(inputConsentId);
        storage.saveCodeVerifier(inputCodeVerifier);
        storage.saveToken(inputToken);

        String outputConsentId = storage.getConsentId();
        String outputCodeVerifier = storage.getCodeVerifier();
        OAuth2Token outputToken = storage.getToken();

        // then
        assertThat(outputConsentId).isEqualTo(inputConsentId);
        assertThat(outputCodeVerifier).isEqualTo(inputCodeVerifier);
        assertThat(outputToken).isEqualTo(inputToken);
    }

    @Test
    public void shouldThrowSessionExceptionWhenNoTokenStored() {
        // given
        SpardaStorage storage = new SpardaStorage(new PersistentStorage(), new SessionStorage());

        // when
        Throwable throwable = catchThrowable(storage::getToken);

        // then
        assertThat(throwable)
                .isInstanceOf(SessionException.class)
                .hasMessage("Access token not found in storage when expected!");
    }
}
