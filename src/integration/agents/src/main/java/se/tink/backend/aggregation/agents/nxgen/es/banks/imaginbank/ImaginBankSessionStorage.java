package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class ImaginBankSessionStorage {
    private final SessionStorage sessionStorage;

    public ImaginBankSessionStorage(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    public void setUserName(String userName) {
        Preconditions.checkNotNull(userName);
        sessionStorage.put(ImaginBankConstants.Storage.USER_NAME, userName);
    }

    public HolderName getHolderName() {
        return new HolderName(sessionStorage.get(ImaginBankConstants.Storage.USER_NAME));
    }
}
