package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.OAuth2StorageMigrator;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcPersistedData;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class KbcStorageMigrator extends OAuth2StorageMigrator {

    public KbcStorageMigrator(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public AgentAuthenticationPersistedData migrate(PersistentStorage persistentStorage) {
        AgentAuthenticationPersistedData agentAuthenticationPersistedData =
                super.migrate(persistentStorage);
        return migrateKbcSpecificData(agentAuthenticationPersistedData, persistentStorage);
    }

    private AgentAuthenticationPersistedData migrateKbcSpecificData(
            AgentAuthenticationPersistedData agentAuthenticationPersistedData,
            PersistentStorage persistentStorage) {
        KbcPersistedData persistedData =
                new KbcPersistedDataAccessorFactory(objectMapper)
                        .createKbcAuthenticationPersistedDataAccessor(
                                agentAuthenticationPersistedData);
        KbcAuthenticationData authenticationData = persistedData.getKbcAuthenticationData();
        Optional.ofNullable(persistentStorage.get(BerlinGroupConstants.StorageKeys.CODE_VERIFIER))
                .ifPresent(codeVierfier -> authenticationData.setCodeVerifier(codeVierfier));
        Optional.ofNullable(persistentStorage.get(BerlinGroupConstants.StorageKeys.CONSENT_ID))
                .ifPresent(consentId -> authenticationData.setConsentId(consentId));
        return persistedData.storeKbcAuthenticationData(authenticationData);
    }
}
