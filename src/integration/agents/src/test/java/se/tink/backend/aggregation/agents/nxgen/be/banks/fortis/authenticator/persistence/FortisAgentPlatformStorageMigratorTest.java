package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class FortisAgentPlatformStorageMigratorTest {

    private FortisAgentPlatformStorageMigrator migrator;
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        migrator = new FortisAgentPlatformStorageMigrator(objectMapper);
    }

    @Test
    public void shouldMigrateWithLegacyCredentials() throws Exception {
        // given
        PersistentStorage persistentStorage = new PersistentStorage();
        persistentStorage.put("agreementId", "testAgreementId");
        persistentStorage.put("password", "testPassword");
        persistentStorage.put("devicefingerprint", "testDeviceFingerprint");
        persistentStorage.put("muid", "testMuid");

        persistentStorage.put("accountProductId", "testCardNumber");
        persistentStorage.put("smid", "testClientId");

        // when
        AgentAuthenticationPersistedData authData = migrator.migrate(persistentStorage);

        // then
        assertThat(authData).isNotNull();
        Map<String, String> authDataMap = authData.valuesCopy();
        assertThat(authDataMap).isNotNull().hasSize(1);
        String serialized = authDataMap.get("FortisAuthData");

        FortisAuthData fortisAuthData = objectMapper.readValue(serialized, FortisAuthData.class);

        assertThat(fortisAuthData).isNotNull();

        assertThat(fortisAuthData.getUsername()).isEqualTo("testCardNumber");
        assertThat(fortisAuthData.getClientNumber()).isEqualTo("testClientId");
        assertThat(fortisAuthData.hasLegacyCredentials()).isTrue();
        assertThat(fortisAuthData.getLegacyAuthData().getAgreementId())
                .isEqualTo("testAgreementId");
        assertThat(fortisAuthData.getLegacyAuthData().getPassword()).isEqualTo("testPassword");
        assertThat(fortisAuthData.getLegacyAuthData().getMuid()).isEqualTo("testMuid");
        assertThat(fortisAuthData.getLegacyAuthData().getDeviceFingerprint())
                .isEqualTo("testDeviceFingerprint");
    }

    @Test
    public void shouldMigrateWithoutLegacyCredentials() throws Exception {
        // given
        PersistentStorage persistentStorage = new PersistentStorage();
        persistentStorage.put("accountProductId", "testCardNumber");
        persistentStorage.put("smid", "testClientId");

        // when
        AgentAuthenticationPersistedData authData = migrator.migrate(persistentStorage);

        // then
        assertThat(authData).isNotNull();
        Map<String, String> authDataMap = authData.valuesCopy();
        assertThat(authDataMap).isNotNull().hasSize(1);
        String serialized = authDataMap.get("FortisAuthData");

        FortisAuthData fortisAuthData = objectMapper.readValue(serialized, FortisAuthData.class);

        assertThat(fortisAuthData).isNotNull();

        assertThat(fortisAuthData.getUsername()).isEqualTo("testCardNumber");
        assertThat(fortisAuthData.getClientNumber()).isEqualTo("testClientId");
        assertThat(fortisAuthData.hasLegacyCredentials()).isFalse();
        assertThat(fortisAuthData.getLegacyAuthData()).isNull();
    }

    @Test
    public void givenIncompleteLegacyCredentials_thenShouldMigrateWithoutLegacyCredentials()
            throws Exception {
        // given
        PersistentStorage persistentStorage = new PersistentStorage();
        persistentStorage.put("agreementId", "testAgreementId");
        persistentStorage.put("password", "testPassword");

        persistentStorage.put("accountProductId", "testCardNumber");
        persistentStorage.put("smid", "testClientId");

        // when
        AgentAuthenticationPersistedData authData = migrator.migrate(persistentStorage);

        // then
        assertThat(authData).isNotNull();
        Map<String, String> authDataMap = authData.valuesCopy();
        assertThat(authDataMap).isNotNull().hasSize(1);
        String serialized = authDataMap.get("FortisAuthData");

        FortisAuthData fortisAuthData = objectMapper.readValue(serialized, FortisAuthData.class);

        assertThat(fortisAuthData).isNotNull();

        assertThat(fortisAuthData.getUsername()).isEqualTo("testCardNumber");
        assertThat(fortisAuthData.getClientNumber()).isEqualTo("testClientId");
        assertThat(fortisAuthData.hasLegacyCredentials()).isFalse();
        assertThat(fortisAuthData.getLegacyAuthData()).isNull();
    }
}
