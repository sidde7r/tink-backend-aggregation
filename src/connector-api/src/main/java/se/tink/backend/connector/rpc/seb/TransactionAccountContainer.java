package se.tink.backend.connector.rpc.seb;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import se.tink.backend.connector.rpc.TransactionContainerType;
import se.tink.backend.utils.LogUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionAccountContainer {

    private static final LogUtils log = new LogUtils(TransactionAccountContainer.class);
    
    @ApiModelProperty(name = "type", value="Indicating if this a historical batch of transactions or a real time transaction.", allowableValues = TransactionContainerType.DOCUMENTED, example = "REAL_TIME", required = true)
    private TransactionContainerType type;
    @ApiModelProperty(name = "transactionAccounts", value="The transaction accounts.", required = true)
    private List<TransactionAccountEntity> transactionAccounts;
    
    public List<TransactionAccountEntity> getTransactionAccounts() {
        return transactionAccounts;
    }
    
    public TransactionContainerType getType() {
        return type;
    }
    
    /**
     * Check that all required fields are set and valid.
     */
    @JsonIgnore
    public boolean isValid(String externalUserId) {
        
        if (type == null) {
            log.info("'type' is null for user " + externalUserId);
            return false;            
        }
        
        if (transactionAccounts == null || transactionAccounts.isEmpty()) {
            log.info("'transactionAccounts' is null or empty for user " + externalUserId);
            return false;            
        }
        
        for (TransactionAccountEntity transactionAccount : transactionAccounts) {
            if (transactionAccount == null || !transactionAccount.isValid(externalUserId)) {
                return false;
            }
        }
        
        return true;
    }
    
    public void setTransactionAccounts(List<TransactionAccountEntity> transactionAccounts) {
        this.transactionAccounts = transactionAccounts;
    }
    
    public void setType(TransactionContainerType type) {
        this.type = type;
    }
}
