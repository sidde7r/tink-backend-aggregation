package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi;

import com.google.gson.Gson;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAuthContext;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiTransactionalAccountsInfo;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BancoBpiEntityManager {

    private static final String PERSISTENCE_STORAGE_KEY = "BancoBpiUserState";
    private static final String PERSISTENCE_ACCOUNT_TRANSACTIONS_KEY =
            "BancoBpiTransactionalAccounts";

    private static final Gson gson = new Gson();
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private BancoBpiAuthContext authContext;
    private BancoBpiTransactionalAccountsInfo transactionalAccounts;

    public BancoBpiEntityManager(
            PersistentStorage persistentStorage, SessionStorage sessionStorage) {
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
    }

    public BancoBpiAuthContext getAuthContext() {
        if (authContext == null) {
            authContext = loadUserState();
        }
        return authContext;
    }

    private BancoBpiAuthContext loadUserState() {
        return Optional.ofNullable(persistentStorage.get(PERSISTENCE_STORAGE_KEY))
                .map(obj -> gson.fromJson(obj, BancoBpiAuthContext.class))
                .orElse(new BancoBpiAuthContext());
    }

    public BancoBpiTransactionalAccountsInfo getTransactionalAccounts() {
        if (transactionalAccounts == null) {
            transactionalAccounts = loadTransactionalAccounts();
        }
        return transactionalAccounts;
    }

    private BancoBpiTransactionalAccountsInfo loadTransactionalAccounts() {
        return Optional.ofNullable(sessionStorage.get(PERSISTENCE_ACCOUNT_TRANSACTIONS_KEY))
                .map(obj -> gson.fromJson(obj, BancoBpiTransactionalAccountsInfo.class))
                .orElse(new BancoBpiTransactionalAccountsInfo());
    }

    private void saveAuthContext() {
        persistentStorage.put(PERSISTENCE_STORAGE_KEY, gson.toJson(authContext));
    }

    private void saveTransactionalAccounts() {
        sessionStorage.put(
                PERSISTENCE_ACCOUNT_TRANSACTIONS_KEY, gson.toJson(transactionalAccounts));
    }

    void saveEntities() {
        saveAuthContext();
        saveTransactionalAccounts();
    }
}
