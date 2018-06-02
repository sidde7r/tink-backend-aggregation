package se.tink.backend.connector.rpc.seb;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import se.tink.backend.utils.LogUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeleteTransactionAccountEntity {

    private static final LogUtils log = new LogUtils(DeleteTransactionAccountEntity.class);
    
    @ApiModelProperty(name = "balance", value="The balance of the account for the time of the last transaction in the list.", example = "19987.5", required = true)
    private Double balance;
    @ApiModelProperty(name = "disposableAmount", value="The disposable amount of the account for the time of the last transaction in the list.", example = "12354.75", required = false)
    private Double disposableAmount;
    @ApiModelProperty(name = "externalId", value="Persistent identifier for the account the transaction belong to.", example = "2d3bd65493b549e1927d97a2d0683ab9", required = true)
    private String externalId;
    @ApiModelProperty(name = "transactions", value="The transaction list.", required = true)
    private List<DeleteTransactionEntity> transactions;
    @ApiModelProperty(name = "payload", value = "The payload property can include arbitrary metadata provided by the financial institution in question that can be used either for deep-linking back to the app of the financial institution, for displaying additional information about the account, or for backend purposes such as automatic categorization improvement, etc. The format is key-value, where key is a String and value any object.")
    private Map<String, Object> payload;

    public Double getBalance() {
        return balance;
    }
    
    public Double getDisposableAmount() {
        return disposableAmount;
    }

    public String getExternalId() {
        return externalId;
    }
    
    public List<DeleteTransactionEntity> getTransactions() {
        return transactions;
    }
    
    /**
     * Check that all required fields are set and valid.
     */
    @JsonIgnore
    public boolean isValid(String externalUserId) {
        if (balance == null) {
            if (payload == null || !Objects.equals(payload.get(PartnerAccountPayload.IGNORE_BALANCE), true)) {
                log.info("'balance' is null for user " + externalUserId);
                return false;
            }
        }
        
        if (Strings.isNullOrEmpty(externalId)) {
            log.info("'externalId' is null or empty for user " + externalUserId);
            return false;
        }
        
        if (transactions == null || transactions.isEmpty()) {
            log.info("'transactions' is null or empty for user " + externalUserId);
            return false;            
        }
        
        for (DeleteTransactionEntity transaction : transactions) {
            if (transaction == null || !transaction.isValid(externalUserId)) {
                return false;
            }
        }
        
        return true;
    }
    
    public void setBalance(Double balance) {
        this.balance = balance;
    }
    
    public void setDisposableAmount(Double disposableAmount) {
        this.disposableAmount = disposableAmount;
    }
    
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
    
    public void setTransactions(List<DeleteTransactionEntity> transactions) {
        this.transactions = transactions;
    }

    public Map<String, Object> getPayload() {
        return payload == null ? Maps.newHashMap() : payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
