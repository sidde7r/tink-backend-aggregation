package se.tink.backend.connector.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PartnerTransactionPayload {

    public static final String PENDING_IDS = "PENDING_IDS";
    public static final String TAGS = "TAGS";
    public static final String PENDING_TRANSACTION_EXPIRATION_DATE = "PENDING_TRANSACTION_EXPIRATION_DATE";

    // The below fields are never used but sent by current partners and might be used in the future. If future partners
    // add the same information, we can make sure that we are consistent with naming and don't have e.g. "CARD_NR" and
    // "CARD_NUMBER".
    public static final String CUSTOMER = "CUSTOMER";
    public static final String CARD_HOLDER = "CARD_HOLDER";
    public static final String ADMIN_TYPE = "ADMIN_TYPE";
    public static final String CARD_NUMBER = "CARD_NUMBER";
    public static final String AMOUNT_CURRENCY = "AMOUNT_CURRENCY";
    public static final String VAT_CURRENCY = "VAT_CURRENCY";
    public static final String CURRENCY = "CURRENCY";
    public static final String CARD_ACTIVITY = "CARD_ACTIVITY";
    public static final String MCC = "MCC";
    public static final String MERCHANT = "MERCHANT";
    public static final String CITY = "CITY";
    public static final String COUNTRY = "COUNTRY";
    public static final String VERIFICATION_NUMBER = "VERIFICATION_NUMBER";
    public static final String REPLAY = "REPLAY";

    @JsonProperty(PENDING_IDS)
    private List<String> pendingIds;
    @JsonProperty(TAGS)
    private List<String> tags;
    @JsonProperty(PENDING_TRANSACTION_EXPIRATION_DATE)
    private Date pendingTransactionExpirationDate;

    public List<String> getPendingIds() {
        return pendingIds == null ? Lists.newArrayList() : pendingIds;
    }

    public void setPendingIds(List<String> pendingIds) {
        this.pendingIds = pendingIds;
    }

    public List<String> getTags() {
        return tags == null ? Lists.newArrayList() : tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Date getPendingTransactionExpirationDate() {
        return pendingTransactionExpirationDate;
    }

    public void setPendingTransactionExpirationDate(Date pendingTransactionExpirationDate) {
        this.pendingTransactionExpirationDate = pendingTransactionExpirationDate;
    }

    public static PartnerTransactionPayload createFromReservationIds(String... ids) {
        PartnerTransactionPayload payload = new PartnerTransactionPayload();
        payload.setPendingIds(Lists.newArrayList(ids));
        return payload;
    }
}
