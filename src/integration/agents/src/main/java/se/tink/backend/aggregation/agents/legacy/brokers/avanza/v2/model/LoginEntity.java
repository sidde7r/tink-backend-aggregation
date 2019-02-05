package se.tink.backend.aggregation.agents.brokers.avanza.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginEntity {
    private String loginPath;
    private String customerId;
    private String username;
    private List<AccountSummaryEntity> accounts;

    public String getLoginPath() {
        return loginPath;
    }

    public void setLoginPath(String loginPath) {
        this.loginPath = loginPath;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<AccountSummaryEntity> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountSummaryEntity> accounts) {
        this.accounts = accounts;
    }
}
