package se.tink.backend.connector.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.google.common.collect.Maps;
import io.swagger.annotations.ApiModelProperty;
import java.util.Map;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import se.tink.backend.core.AccountTypes;
import se.tink.libraries.http.annotations.validation.StringNotNullOrEmpty;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class AccountEntity {

    @ApiModelProperty(value = "The available credit of the account.", example = "20000.0")
    private Double availableCredit;

    @NotNull
    @ApiModelProperty(value = "The balance of the account.", example = "7000.0", required = true)
    private Double balance;

    @ApiModelProperty(value = "The currently reserved amount of the account.", example = "2000.0")
    private Double reservedAmount;

    @StringNotNullOrEmpty
    @ApiModelProperty(value = "Persistent identifier for the account.", example = "2d3bd65493b549e1927d97a2d0683ab9", required = true)
    private String externalId;

    @StringNotNullOrEmpty
    @ApiModelProperty(value = "The account name.", example = "Enkla sparkontot", required = true)
    private String name;

    @StringNotNullOrEmpty
    @ApiModelProperty(value = "The account number.", example = "52670208126", required = true)
    private String number;

    @ApiModelProperty(value = "The payload property can include arbitrary metadata provided by the financial institution in question that can be used either for deep-linking back to the app by the financial institution, for displaying additional information about the account, etc. The format is key-value, where key is a String and value any object.", required = false, example = "{}")
    private Map<String, Object> payload;

    @NotNull
    @ApiModelProperty(value = "The account type.", example = "CREDIT_CARD", allowableValues = AccountTypes.DOCUMENTED, required = true)
    private AccountTypes type;

    @AssertTrue(message = "If " + PartnerAccountPayload.CALCULATE_BALANCE + " is set, the balance must start from 0 and all historical transactions must be sent to Tink after creating the account to ensure a correct balance.")
    private boolean isZeroBalanceIfCalculateBalanceRequested() {
        if (payload == null) {
            return true;
        }

        if (payload.containsKey(PartnerAccountPayload.CALCULATE_BALANCE)) {
            return balance != null && balance == 0 && (reservedAmount == null || reservedAmount == 0);
        }

        return true;
    }

    public Double getAvailableCredit() {
        return availableCredit;
    }

    public Double getBalance() {
        return balance;
    }

    public Double getReservedAmount() {
        return reservedAmount;
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
        return payload == null ? Maps.newHashMap() : payload;
    }

    public AccountTypes getType() {
        return type;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setType(AccountTypes type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public void setAvailableCredit(Double availableCredit) {
        this.availableCredit = availableCredit;
    }

    public void setReservedAmount(Double reservedAmount) {
        this.reservedAmount = reservedAmount;
    }
}
