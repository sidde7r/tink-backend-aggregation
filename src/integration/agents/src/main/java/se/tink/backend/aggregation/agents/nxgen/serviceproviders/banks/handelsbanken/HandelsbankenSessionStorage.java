package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.entities.HandelsbankenCreditCard;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.rpc.AccountListResponse;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class HandelsbankenSessionStorage {
    private final SessionStorage sessionStorage;
    private final HandelsbankenConfiguration configuration;
    private final LogMasker logMasker;

    public HandelsbankenSessionStorage(
            HandelsbankenConfiguration handelsbankenConfiguration,
            SessionStorage sessionStorage,
            LogMasker logMasker) {
        this.sessionStorage = sessionStorage;
        this.configuration = handelsbankenConfiguration;
        this.logMasker = logMasker;
    }

    public void persist(ApplicationEntryPointResponse applicationEntryPoint) {
        persist(HandelsbankenConstants.Storage.APPLICATION_ENTRY_POINT, applicationEntryPoint);
        logMasker.mask(applicationEntryPoint.getAuthToken());
    }

    public Optional<ApplicationEntryPointResponse> applicationEntryPoint() {
        return retrieve(
                HandelsbankenConstants.Storage.APPLICATION_ENTRY_POINT,
                ApplicationEntryPointResponse.class);
    }

    public void removeApplicationEntryPoint() {
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
        return this.sessionStorage.get(key, valueType);
    }

    private void persist(String key, Object value) {
        this.sessionStorage.put(key, value, false);
    }

    private void remove(String key) {
        this.sessionStorage.remove(key);
    }
}
