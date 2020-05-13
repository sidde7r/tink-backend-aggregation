package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.entities.HandelsbankenCreditCard;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.rpc.AccountListResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class HandelsbankenSessionStorage {
    private final SessionStorage sessionStorage;
    private final HandelsbankenConfiguration configuration;
    private static final Logger log = LoggerFactory.getLogger(HandelsbankenApiClient.class);

    public HandelsbankenSessionStorage(HandelsbankenConfiguration configuration) {
        this.sessionStorage = new SessionStorage();
        this.configuration = configuration;
    }

    protected HandelsbankenSessionStorage(HandelsbankenSessionStorage sessionStorage) {
        this(sessionStorage.configuration);
    }

    public void persist(ApplicationEntryPointResponse applicationEntryPoint) {
        log.info("going to persist applicationEntryPoint");
        persist(HandelsbankenConstants.Storage.APPLICATION_ENTRY_POINT, applicationEntryPoint);
    }

    public Optional<ApplicationEntryPointResponse> applicationEntryPoint() {
        log.info("going to retrieve applicationEntryPoint");
        return retrieve(
                HandelsbankenConstants.Storage.APPLICATION_ENTRY_POINT,
                ApplicationEntryPointResponse.class);
    }

    public void removeApplicationEntryPoint() {
        log.info("going to remove applicationEntryPoint");
        remove(HandelsbankenConstants.Storage.APPLICATION_ENTRY_POINT);
    }

    public void persist(AccountListResponse accountList) {
        persist(HandelsbankenConstants.Storage.ACCOUNT_LIST, accountList);
    }

    public Optional<? extends AccountListResponse> accountList() {
        return retrieve(
                HandelsbankenConstants.Storage.ACCOUNT_LIST,
                configuration.getAccountListResponse());
    }

    public void persist(CreditCardsResponse cards) {
        persist(HandelsbankenConstants.Storage.CREDIT_CARDS, cards);
    }

    public <CreditCard extends HandelsbankenCreditCard>
            Optional<CreditCardsResponse<CreditCard>> creditCards() {
        return retrieve(
                HandelsbankenConstants.Storage.CREDIT_CARDS,
                configuration.getCreditCardsResponse());
    }

    private <T> Optional<T> retrieve(String key, Class<T> valueType) {
        Optional<T> value = this.sessionStorage.get(key, valueType);
        if (value.isPresent()) {
            log.info("in retrieve key: {}, valueType: {}, value: {}", key, valueType, value.get());
        } else {
            log.info("in retrieve key: {}, valueType: {}, no value", key, valueType);
        }

        return value;
    }

    private void persist(String key, Object value) {
        log.info("in persist key: {}, value: {}, no value", key, value);
        this.sessionStorage.put(key, value);
    }

    private void remove(String key) {
        log.info("removing key: {} ", key);
        this.sessionStorage.remove(key);
    }
}
