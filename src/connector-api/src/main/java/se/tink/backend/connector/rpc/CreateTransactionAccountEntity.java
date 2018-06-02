package se.tink.backend.connector.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.google.common.collect.Maps;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import se.tink.libraries.http.annotations.validation.ListNotNullOrEmpty;
import se.tink.libraries.http.annotations.validation.NoNullElements;
import se.tink.libraries.http.annotations.validation.StringNotNullOrEmpty;

@SuppressWarnings("unused")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CreateTransactionAccountEntity implements TransactionAccountEntity {

    @ApiModelProperty(value = "The balance of the account for the time of the last transaction in the list.", example = "7000.0", required = true)
    private Double balance;

    @ApiModelProperty(value = "The reserved amount of the account for the time of the last transaction in the list.", example = "2000.0", required = false)
    private Double reservedAmount;

    @StringNotNullOrEmpty
    @ApiModelProperty(value = "Persistent identifier for the account the transaction belongs to.", example = "2d3bd65493b549e1927d97a2d0683ab9", required = true)
    private String externalId;

    @ListNotNullOrEmpty
    @NoNullElements
    @Valid
    @ApiModelProperty(value = "The transaction list.", required = true)
    private List<CreateTransactionEntity> transactions;

    @ApiModelProperty(value = "The payload property can include arbitrary metadata provided by the financial institution in question that can be used either for deep-linking back to the app of the financial institution, for displaying additional information about the account, or for backend purposes such as automatic categorization improvement, etc. The format is key-value, where key is a String and value any object.", required = false, example = "{}")
    private Map<String, Object> payload;

    @AssertTrue(message = "may not be null")
    private boolean isValidBalance() {
        if (balance == null) {
            if (payload == null) {
                return false;
            }

            if (!Objects.equals(payload.get(PartnerAccountPayload.IGNORE_BALANCE), true) && !Objects
                    .equals(payload.get(PartnerAccountPayload.CALCULATE_BALANCE), true)) {
                return false;
            }
        }
        return true;
    }

    @AssertTrue(message = "must have a list of transactions with unique externalId:s")
    private boolean isUniqueExternalIds() {
        if (transactions == null) {
            return true;
        }

        Set<String> uniqueIds = transactions.stream().map(CreateTransactionEntity::getExternalId).collect(Collectors.toSet());
        return uniqueIds.size() == transactions.size();
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

    public List<CreateTransactionEntity> getTransactions() {
        return transactions;
    }

    public Map<String, Object> getPayload() {
        return payload == null ? Maps.newHashMap() : payload;
    }

    public void setTransactions(List<CreateTransactionEntity> transactions) {
        this.transactions = transactions;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public void setReservedAmount(Double reservedAmount) {
        this.reservedAmount = reservedAmount;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
