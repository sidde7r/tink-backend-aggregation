package se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria;

import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BankAustriaSessionStorage {
    private SessionStorage sessionStorage;

    public BankAustriaSessionStorage(
            SessionStorage sessionStorage, String iphone7OtmlLayoutInitial) {
        this.sessionStorage = sessionStorage;
        setXOtmlManifest(iphone7OtmlLayoutInitial);
    }

    public String getXOtmlManifest() {
        return sessionStorage.get(BankAustriaConstants.Header.MANIFEST);
    }

    public void setXOtmlManifest(String manifestVersion) {
        sessionStorage.put(BankAustriaConstants.Header.MANIFEST, manifestVersion);
    }
}
