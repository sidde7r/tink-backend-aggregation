package se.tink.backend.connector.rpc.seb;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.utils.LogUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {

    private static final LogUtils log = new LogUtils(TransactionEntity.class);
    private static final Date ZERO_DATE = new Date(0);

    @ApiModelProperty(name = "amount", value = "The debited/credited amount in the currency of the account.", example = "-98.50", required = true)
    private Double amount;
    @ApiModelProperty(name = "date", value = "Date is when the transaction was executed, not when it was settled (except for scheduled transfers/payments, where the settling date is to be interpreted as the execution date).", example = "1455740874875", required = true)
    private Date date;
    @ApiModelProperty(name = "description", value = "A merchant name if possible. If such value is not available, the description that is shown in the transaction list.", example = "Riche Teatergrillen", required = true)
    private String description;
    @ApiModelProperty(name = "externalId", value = "Persistent identifier for the transaction.", example = "40dc04e5353547378c84f34ffc88f853", required = true)
    private String externalId;
    @ApiModelProperty(name = "payload", value = "The payload property can include arbitrary metadata provided by the financial institution in question that can be used either for deep-linking back to the app of the financial institution, for displaying additional information about the transaction, or for backend purposes such as automatic categorization improvement, etc. The format is key-value, where key is a String and value any object.")
    private Map<String, Object> payload;
    @ApiModelProperty(name = "type", value = "The type of the transaction.", allowableValues = TransactionTypes.DOCUMENTED, example = "CREDIT_CARD", required = true)
    private TransactionTypes type;
    @ApiModelProperty(name = "status", value = "Status of transaction.", allowableValues = TransactionStatus.DOCUMENTED, example = "BOOKED", required = false)
    private TransactionStatus status;

    private Date entityCreated;

    public TransactionEntity() {
        // Timestamp of when the entity was created. Used to measure the time of the "whole processing chain" from when
        // we received a transaction in the connector until it is saved and statistics and activities are generated.
        entityCreated = new Date();
    }

    public Double getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getExternalId() {
        return externalId;
    }

    public Map<String, Object> getPayload() {
        return payload == null ? Maps.newHashMap() : payload;
    }

    public TransactionTypes getType() {
        return type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    /**
     * Check that all required fields are set and valid.
     */
    @JsonIgnore
    public boolean isValid(String externalUserId) {
        if (amount == null) {
            log.info("'amount' is null for user " + externalUserId);
            return false;
        }

        if (date == null) {
            log.info("'date' is null for user " + externalUserId);
            return false;
        } else if (date.before(ZERO_DATE)) {
            log.info("'date' is 0 for user " + externalUserId);
            return false;
        }

        if (Strings.isNullOrEmpty(description)) {
            log.info("'description' is null or empty for user " + externalUserId);
            return false;
        }

        if (Strings.isNullOrEmpty(externalId)) {
            log.info("'externalId' is null or empty for user " + externalUserId);
            return false;
        }

        if (type == null) {
            log.info("'type' is null for user " + externalUserId);
            return false;
        }

        if (payload != null) {
            Object expiration = payload.get(PartnerTransactionPayload.PENDING_TRANSACTION_EXPIRATION_DATE);

            if (expiration != null) {
                if (!Objects.equals(status, TransactionStatus.RESERVED)) {
                    log.info("Pending expiration date set, but transaction is not RESERVED for user " + externalUserId);
                    return false;
                }

                if (!(expiration instanceof Long)) {
                    log.info("Expiration Date is not a Date for user " + externalUserId);
                    return false;
                }
            }
        }

        return true;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public void setType(TransactionTypes type) {
        this.type = type;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("amount", getAmount())
                .add("date", getDate())
                .add("description", getDescription())
                .toString();
    }

    public Date getEntityCreated() {
        return entityCreated;
    }
}
