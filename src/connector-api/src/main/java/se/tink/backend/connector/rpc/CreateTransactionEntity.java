package se.tink.backend.connector.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import se.tink.backend.connector.utils.TagValidationUtils;
import se.tink.backend.core.TransactionTypes;
import se.tink.libraries.http.annotations.validation.StringNotNullOrEmpty;
import se.tink.libraries.http.annotations.validation.ValidDate;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CreateTransactionEntity implements TransactionEntity {

    @NotNull
    @ApiModelProperty(value = "The debited/credited amount in the currency of the account.", example = "-98.50", required = true)
    private Double amount;

    @NotNull
    @ValidDate
    @ApiModelProperty(value = "Date is when the transaction was executed, not when it was settled (except for scheduled transfers/payments, where the settling date is to be interpreted as the execution date).", example = "1455740874875", required = true)
    private Date date;

    @StringNotNullOrEmpty
    @ApiModelProperty(value = "A merchant name if possible. If such value is not available, the description that is shown in the transaction list.", example = "Riche Teatergrillen", required = true)
    private String description;

    @ApiModelProperty(value = "Persistent identifier for the transaction.", example = "40dc04e5353547378c84f34ffc88f853", required = true)
    private String externalId;

    @ApiModelProperty(value = "Ignored for new objects. Used to specify the id as given by Tink on when updating objects without an existing external ID.")
    private String tinkId;

    @ApiModelProperty(value = "The payload property can include arbitrary metadata provided by the financial institution in question that can be used either for deep-linking back to the app of the financial institution, for displaying additional information about the transaction, or for backend purposes such as automatic categorization improvement, etc. The format is key-value, where key is a String and value any object.", required = false, example = "{}")
    private Map<String, Object> payload;

    @NotNull
    @ApiModelProperty(value = "The type of the transaction.", allowableValues = TransactionTypes.DOCUMENTED, example = "CREDIT_CARD", required = true)
    private TransactionTypes type;

    @ApiModelProperty(value = "If the transaction is pending (reserved) or not (booked).")
    private boolean pending;

    @ApiModelProperty(hidden = true, value = "Timestamp of when the entity was created. Used to measure the time of the whole processing chain from when we received a transaction in the connector until it is saved and statistics and activities are generated.")
    private Date entityCreated = new Date();

    @AssertTrue(message = "Payload is not deserializable")
    private boolean isPayloadDeserializable() {
        String serializedPayload = SerializationUtils.serializeToString(getPayload());
        PartnerTransactionPayload payload = SerializationUtils.deserializeFromString(
                serializedPayload, PartnerTransactionPayload.class);

        return payload != null;
    }

    @AssertTrue(message = "Pending expiration date set, but transaction is not pending")
    private boolean isNotPendingButExpirationDateIsSet() {
        if (pending) {
            return true;
        }

        if (payload == null) {
            return true;
        }

        // Transaction is not pending, then payload cannot include expiration date
        return !payload.containsKey(PartnerTransactionPayload.PENDING_TRANSACTION_EXPIRATION_DATE);
    }

    @AssertTrue(message = "tags must be a list of strings")
    private boolean isPayloadTagsListOfStrings() {
        Object rawTags = this.getPayload().get(PartnerTransactionPayload.TAGS);
        if (rawTags == null) {
            return true; // No tags
        }

        return TagValidationUtils.asTagList(rawTags).isPresent();
    }

    @AssertTrue(message = "tags must be alphanumeric without whitespace")
    private boolean isPayloadTagsAlphanumeric() {
        Object rawTags = this.getPayload().get(PartnerTransactionPayload.TAGS);
        if (rawTags == null) {
            return true; // No tags
        }

        Optional<List<String>> tags = TagValidationUtils.asTagList(rawTags);
        if (!tags.isPresent()) {
            return true;
        }

        return tags.get().stream().allMatch(t -> TagValidationUtils.normalizeTag(t).isPresent());
    }

    @AssertTrue(message = "Either tinkId or externalId must be specified.")
    private boolean isTinkIdOrExternalIdSet() {
        return tinkId != null || externalId != null;
    }

    @AssertTrue(message = "Found both externalId and tinkId. Only one may be used.")
    private boolean isTinkIdExclusiveOrExternalId() {
        return tinkId != null ^ externalId != null;
    }

    public double getAmount() {
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

    public String getTinkId() {
        return tinkId;
    }

    public Map<String, Object> getPayload() {
        return payload == null ? Maps.newHashMap() : payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public TransactionTypes getType() {
        return type;
    }

    public Date getEntityCreated() {
        return entityCreated;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("amount", getAmount())
                .add("date", getDate())
                .add("description", getDescription())
                .toString();
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setType(TransactionTypes type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }
}
