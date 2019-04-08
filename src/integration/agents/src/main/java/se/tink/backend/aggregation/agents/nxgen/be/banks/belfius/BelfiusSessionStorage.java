package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius;

import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BelfiusSessionStorage {

    private final SessionStorage sessionStorage;

    public BelfiusSessionStorage(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    public void clearSessionData() {
        this.sessionStorage.remove(BelfiusConstants.Storage.SESSION_ID);
        this.sessionStorage.remove(BelfiusConstants.Storage.MACHINE_IDENTIFIER);
        this.sessionStorage.remove(BelfiusConstants.Storage.REQUEST_COUNTER);
    }

    public boolean containsSessionData() {
        return this.sessionStorage.containsKey(BelfiusConstants.Storage.SESSION_ID);
    }

    public void putSessionData(String sessionId, String machineIdentifier) {
        this.sessionStorage.put(BelfiusConstants.Storage.SESSION_ID, sessionId);
        this.sessionStorage.put(BelfiusConstants.Storage.MACHINE_IDENTIFIER, machineIdentifier);
        this.sessionStorage.remove(BelfiusConstants.Storage.REQUEST_COUNTER);
    }

    public void incrementRequestCounter() {
        if (containsSessionData()) {
            int requestCounter = Integer.valueOf(getRequestCounter()) + 1;
            this.sessionStorage.put(
                    BelfiusConstants.Storage.REQUEST_COUNTER, String.valueOf(requestCounter));
        }
    }

    public String getSessionId() {
        return this.sessionStorage.get(BelfiusConstants.Storage.SESSION_ID);
    }

    public String getMachineIdentifier() {
        return Optional.ofNullable(
                        this.sessionStorage.get(BelfiusConstants.Storage.MACHINE_IDENTIFIER))
                .orElse("XXX");
    }

    public String getRequestCounter() {
        return Optional.ofNullable(
                        this.sessionStorage.get(BelfiusConstants.Storage.REQUEST_COUNTER))
                .orElse("1");
    }

    public String getChallenge() {
        return sessionStorage.get("challenge");
    }

    public void setChallenge(String challenge) {
        sessionStorage.put("challenge", challenge);
    }

    public void setNumberOfAccounts(int size) {
        sessionStorage.put("accounts", Integer.toString(size));
    }

    public int getNumberOfAccounts() {
        String numberOfAccounts = sessionStorage.get("accounts");
        return Strings.isNullOrEmpty(numberOfAccounts) ? 1 : Integer.valueOf(numberOfAccounts);
    }
}
