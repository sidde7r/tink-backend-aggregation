package se.tink.backend.connector.rpc.seb;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;
import io.swagger.annotations.ApiModelProperty;
import java.util.Map;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.utils.LogUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountEntity {

    private static final LogUtils log = new LogUtils(AccountEntity.class);

    @ApiModelProperty(name = "availableCredit", value = "The available credit of the account.", example = "20000")
    private Double availableCredit;
    @ApiModelProperty(name = "balance", value = "The balance of the account.", example = "7645.25", required = true)
    private Double balance;
    @ApiModelProperty(name = "disposableAmount", value = "The disposable amount of the account.", example = "12354.75")
    private Double disposableAmount;
    @ApiModelProperty(name = "externalId", value = "Persistent identifier for the account.", example = "2d3bd65493b549e1927d97a2d0683ab9", required = true)
    private String externalId;
    @ApiModelProperty(name = "name", value = "The account name.", example = "Enkla sparkontot", required = true)
    private String name;
    @ApiModelProperty(name = "number", value = "The account number.", example = "52670208126", required = true)
    private String number;
    @ApiModelProperty(name = "payload", value = "The payload property can include arbitrary metadata provided by the financial instituion in question that can be used either for deep-linking back to the app by the financial institution, for displaying additional information about the account, etc. The format is key-value, where key is a String and value any object.", required = false, example = "{}")
    private Map<String, Object> payload;
    @ApiModelProperty(name = "type", value = "The account type.", example = "CREDIT_CARD", allowableValues = AccountTypes.DOCUMENTED, required = true)
    private AccountTypes type;

    public Double getAvailableCredit() {
        return availableCredit;
    }

    public Double getBalance() {
        return balance;
    }

    public Double getDisposableAmount() {
        return disposableAmount;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public AccountTypes getType() {
        return type;
    }
    
    /**
     * Check that all required fields are set and valid.
     */
    @JsonIgnore
    public boolean isValid(String externalUserId) {
        
        if (balance == null) {
            log.info("'balance' is null for user " + externalUserId);
            return false;
        }
        
        if (Strings.isNullOrEmpty(externalId)) {
            log.info("'externalId' is null or empty for user " + externalUserId);
            return false;
        }
        
        if (Strings.isNullOrEmpty(name)) {
            log.info("'name' is null or empty for user " + externalUserId);
            return false;
        }
        
        if (Strings.isNullOrEmpty(number)) {
            log.info("'number' is null or empty for user " + externalUserId);
            return false;
        }
        
        if (type == null) {
            log.info("'type' is null for user " + externalUserId);
            return false;
        }
        
        return true;
    }

    public void setAvailableCredit(Double availableCredit) {
        this.availableCredit = availableCredit;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public void setType(AccountTypes type) {
        this.type = type;
    }
}
