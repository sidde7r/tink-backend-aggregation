package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance;

import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.LogTags.LUNAR_TAG;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.Storage;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentJsonRepresentationAuthenticationPersistedDataAccessor;

@Slf4j
public class LunarAuthDataAccessor
        extends AgentJsonRepresentationAuthenticationPersistedDataAccessor<LunarAuthData> {

    LunarAuthDataAccessor(
            AgentAuthenticationPersistedData agentAuthenticationPersistedData,
            ObjectMapper objectMapper) {
        super(agentAuthenticationPersistedData, objectMapper, LunarAuthData.class);
    }

    @Override
    protected String storeKey() {
        return Storage.PERSISTED_DATA_KEY;
    }

    public LunarAuthData get() {
        log.info("{} Getting data from the storage", LUNAR_TAG);
        Optional<LunarAuthData> authData = getFromStorage();
        if (!authData.isPresent()) {
            log.info("{} Data in storage is empty", LUNAR_TAG);
        }
        return authData.orElseGet(LunarAuthData::new);
    }

    public AgentAuthenticationPersistedData storeData(LunarAuthData value) {
        log.info("{} Storing data in the storage", LUNAR_TAG);
        return super.store(value);
    }

    public AgentAuthenticationPersistedData clearData() {
        log.info("{} Clearing data from the storage", LUNAR_TAG);
        return super.store(new LunarAuthData());
    }
}
