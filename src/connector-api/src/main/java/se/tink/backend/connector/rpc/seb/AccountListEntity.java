package se.tink.backend.connector.rpc.seb;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import se.tink.backend.utils.LogUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountListEntity {

    private static final LogUtils log = new LogUtils(AccountListEntity.class);
    
    @ApiModelProperty(name = "accounts", value = "The accounts.", required = true)
    private List<AccountEntity> accounts;

    public List<AccountEntity> getAccounts() {
        return accounts;
    }
    
    /**
     * Check that all required fields are set and valid.
     */
    @JsonIgnore
    public boolean isValid(String externalUserId) {
        if (accounts == null || accounts.isEmpty()) {
            log.info("'accounts' is null or empty for user " + externalUserId);
            return false;
        }
        
        for (AccountEntity account : accounts) {
            if (account == null || !account.isValid(externalUserId)) {
                return false;
            }
        }
        
        return true;
    }

    public void setAccounts(List<AccountEntity> accounts) {
        this.accounts = accounts;
    }
}
