package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity;

import com.google.gson.Gson;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.BPostBankAuthContext;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BPostBankEntityManager {

    private static final Gson gson = new Gson();
    private PersistentStorage storage;
    private BPostBankAuthContext authContext;

    public BPostBankEntityManager(PersistentStorage storage) {
        this.storage = storage;
    }

    public BPostBankAuthContext getAuthenticationContext() {
        if (authContext == null) {
            authContext =
                    Optional.ofNullable(storage.get(BPostBankAuthContext.class.getSimpleName()))
                            .map(v -> gson.fromJson(v, BPostBankAuthContext.class))
                            .orElse(new BPostBankAuthContext());
        }
        return authContext;
    }

    public void save() {
        storage.put(BPostBankAuthContext.class.getSimpleName(), gson.toJson(authContext));
    }
}
