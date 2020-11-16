package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo;

import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.SodexoConstants.Storage;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SodexoStorage {

    private PersistentStorage persistentStorage;
    private SessionStorage sessionStorage;

    public SodexoStorage(PersistentStorage persistentStorage, SessionStorage sessionStorage) {
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
    }

    public void setSessionToken(String token) {
        sessionStorage.put(Storage.SESSION_TOKEN, token);
    }

    public String getSessionToken() {
        return sessionStorage.get(Storage.SESSION_TOKEN);
    }

    public void setPin(String pin) {
        persistentStorage.put(Storage.PIN, pin);
    }

    public String getPin() {
        return persistentStorage.get(Storage.PIN);
    }

    public void setUserToken(String userToken) {
        persistentStorage.put(Storage.USER_TOKEN, userToken);
    }

    public String getUserToken() {
        return persistentStorage.get(Storage.USER_TOKEN);
    }

    public boolean isRegistered() {
        return persistentStorage.containsKey(Storage.USER_TOKEN);
    }

    public String getName() {
        return persistentStorage.get(Storage.NAME);
    }

    public void setName(String name) {
        persistentStorage.put(Storage.NAME, name);
    }

    public String getSurname() {
        return persistentStorage.get(Storage.SURNAME);
    }

    public void setSurname(String surname) {
        persistentStorage.put(Storage.SURNAME, surname);
    }

    public String getCardNumber() {
        return persistentStorage.get(Storage.CARD_NUMBER);
    }

    public void setCardNumber(String cardNumber) {
        persistentStorage.put(Storage.CARD_NUMBER, cardNumber);
    }
}
