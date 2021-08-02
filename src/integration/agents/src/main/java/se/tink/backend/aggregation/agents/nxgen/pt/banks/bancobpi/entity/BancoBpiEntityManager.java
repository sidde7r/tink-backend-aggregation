package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity;

import com.google.gson.Gson;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BancoBpiEntityManager {

    private static final String PERSISTENCE_STORAGE_KEY = "BancoBpiUserState";
    private static final String PERSISTENCE_ACCOUNT_TRANSACTIONS_KEY =
            "BancoBpiTransactionalAccounts";
    private static final String PERSISTENCE_PRODUCTS_DATA_KEY = "BancoBpiProductsData";

    private static final Gson gson = new Gson();
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private BancoBpiAuthContext authContext;
    private BancoBpiAccountsContext transactionalAccounts;
    private BancoBpiProductsData productsData;

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

    public BancoBpiAccountsContext getAccountsContext() {
        if (transactionalAccounts == null) {
            transactionalAccounts = loadTransactionalAccounts();
        }
        return transactionalAccounts;
    }

    public Optional<BancoBpiProductsData> getProductsData() {
        if (productsData == null) {
            productsData = loadProductsData();
        }
        return Optional.ofNullable(productsData);
    }

    public void setProductsData(BancoBpiProductsData productsData) {
        this.productsData = productsData;
    }

    private BancoBpiAccountsContext loadTransactionalAccounts() {
        return Optional.ofNullable(sessionStorage.get(PERSISTENCE_ACCOUNT_TRANSACTIONS_KEY))
                .map(obj -> gson.fromJson(obj, BancoBpiAccountsContext.class))
                .orElseGet(BancoBpiAccountsContext::new);
    }

    private BancoBpiProductsData loadProductsData() {
        return Optional.ofNullable(sessionStorage.get(PERSISTENCE_PRODUCTS_DATA_KEY))
                .map(obj -> gson.fromJson(obj, BancoBpiProductsData.class))
                .orElse(null);
    }

    private void saveAuthContext() {
        persistentStorage.put(PERSISTENCE_STORAGE_KEY, gson.toJson(authContext));
    }

    private void saveSaveAccountsContext() {
        if (transactionalAccounts != null) {
            sessionStorage.put(
                    PERSISTENCE_ACCOUNT_TRANSACTIONS_KEY, gson.toJson(transactionalAccounts));
        }
    }

    private void saveProductsData() {
        if (productsData != null) {
            sessionStorage.put(PERSISTENCE_PRODUCTS_DATA_KEY, gson.toJson(productsData));
        }
    }

    public void saveEntities() {
        saveAuthContext();
        saveSaveAccountsContext();
        saveProductsData();
    }
}
