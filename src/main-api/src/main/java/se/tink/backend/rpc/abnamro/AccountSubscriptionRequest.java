package se.tink.backend.rpc.abnamro;

import com.google.common.collect.Lists;
import io.protostuff.Tag;
import java.util.List;

public class AccountSubscriptionRequest implements AuthenticatedRequest {

    @Tag(1)
    private String bcNumber;
    @Tag(2)
    private String sessionToken;
    @Tag(3)
    private List<Long> accounts;

    public List<Long> getAccounts() {

        if (accounts == null) {
            return Lists.newArrayList();
        }

        return accounts;
    }
    
    public String getBcNumber() {
        return bcNumber;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setAccounts(List<Long> accounts) {
        this.accounts = accounts;
    }
    
    public void setBcNumber(String bcNumber) {
        this.bcNumber = bcNumber;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
}
