package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.storage;

import java.util.Set;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants.IdTags;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.TypeReferences;

public class IcaBankenSessionStorage {
    private SessionStorage sessionStorage;

    public IcaBankenSessionStorage(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    public void saveSessionId(String sessionId) {
        sessionStorage.put(IdTags.SESSION_ID_TAG, sessionId);
    }

    public String getSessionId() {
        return sessionStorage.get(IdTags.SESSION_ID_TAG);
    }

    public void savePolicies(Set<String> policies) {
        sessionStorage.put(IdTags.POLICIES_TAG, policies, false);
    }

    public boolean hasPolicy(String policyName) {
        return sessionStorage
                .get(IdTags.POLICIES_TAG, TypeReferences.SET_OF_STRINGS)
                .map(policies -> policies.contains(policyName))
                .orElse(false);
    }
}
