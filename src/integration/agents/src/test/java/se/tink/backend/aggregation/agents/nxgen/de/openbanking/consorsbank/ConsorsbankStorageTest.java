package se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ConsorsbankStorageTest {

    @Test
    public void shouldHoldAllValuesCorrectly() {
        // given
        ConsorsbankStorage storage = new ConsorsbankStorage(new PersistentStorage());
        AccessEntity inputAccessEntity = new AccessEntity();
        String inputConsentId = "test_consent_id";

        // when
        storage.saveConsentAccess(inputAccessEntity);
        storage.saveConsentId(inputConsentId);

        AccessEntity outputAccessEntity = storage.getConsentAccess();
        String outputConsentId = storage.getConsentId();

        // then
        assertThat(outputAccessEntity).isEqualTo(inputAccessEntity);
        assertThat(outputConsentId).isEqualTo(inputConsentId);
    }
}
