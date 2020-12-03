package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.AgentPlatformStorageMigrator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisConstants;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class FortisAgentPlatformStorageMigrator implements AgentPlatformStorageMigrator {

    private static final String PASSWORD = "password";
    private static final String SMID = "smid";
    private static final String AGREEMENT_ID = "agreementId";
    private static final String MUID = "muid";
    private static final String DEVICE_FINGERPRINT = "devicefingerprint";

    private final ObjectMapper objectMapper;

    @Override
    public AgentAuthenticationPersistedData migrate(PersistentStorage persistentStorage) {

        final String agreementId = persistentStorage.get(AGREEMENT_ID);
        final String password = persistentStorage.get(PASSWORD);
        final String deviceFingerprint = persistentStorage.get(DEVICE_FINGERPRINT);
        final String muid = persistentStorage.get(MUID);

        String cardNumber = persistentStorage.get(FortisConstants.Storage.ACCOUNT_PRODUCT_ID);
        String clientNumber = persistentStorage.get(SMID);

        FortisAuthData authData = new FortisAuthData();
        authData.setUsername(cardNumber);
        authData.setClientNumber(clientNumber);

        if (ObjectUtils.allNotNull(agreementId, password, deviceFingerprint, muid)) {
            authData.setLegacyAuthData(
                    new FortisLegacyAuthData(agreementId, password, deviceFingerprint, muid));
        }

        return new FortisAuthDataAccessor(
                        new AgentAuthenticationPersistedData(new HashMap<>()), objectMapper)
                .store(authData);
    }
}
