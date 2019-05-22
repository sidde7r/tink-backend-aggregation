package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.SpankkiConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.entities.CustomerEntity;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SpankkiSessionStorage {

    private final SessionStorage sessionStorage;

    SpankkiSessionStorage(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    public void putSessionId(String sessionId) {
        sessionStorage.put(SpankkiConstants.Storage.SESSION_ID, sessionId);
    }

    public String getSessionId() {
        return Optional.ofNullable(sessionStorage.get(SpankkiConstants.Storage.SESSION_ID))
                .orElse("");
    }

    public void putCustomerId(String customerId) {
        sessionStorage.put(SpankkiConstants.Storage.CUSTOMER_ID, customerId);
    }

    public String getCustomerId() {
        return sessionStorage.get(SpankkiConstants.Storage.CUSTOMER_ID);
    }

    public void putCustomerEntity(CustomerEntity customer) {
        sessionStorage.put(Storage.CUSTOMER_ENTITY, customer);
    }

    public Optional<CustomerEntity> getCustomerEntity() {
        return sessionStorage.get(Storage.CUSTOMER_ENTITY, CustomerEntity.class);
    }
}
