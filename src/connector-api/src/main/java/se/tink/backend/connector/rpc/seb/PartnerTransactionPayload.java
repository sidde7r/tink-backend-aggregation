package se.tink.backend.connector.rpc.seb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings({ "WeakerAccess", "unused" })
public class PartnerTransactionPayload {

    public static final String RESERVATION_ID = "RESERVATION_ID";
    public static final String RESERVATION_IDS = "RESERVATION_IDS";
    public static final String CHECKPOINT_ID = "CHECKPOINT_ID";
    public static final String PENDING_TRANSACTION_EXPIRATION_DATE = "PENDING_TRANSACTION_EXPIRATION_DATE";

    // The below fields are never used but sent by Cornwall and might be used in the future. If future partners add the same
    // information, we can make sure that we are consistent with naming and don't have e.g. "CARD_NR" and "CARD_NUMBER".
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

    @JsonProperty(RESERVATION_ID)
    private String reservationId;

    @JsonProperty(RESERVATION_IDS)
    private List<String> reservationIds;

    @JsonProperty(CHECKPOINT_ID)
    private String checkpointId;

    @JsonProperty(PENDING_TRANSACTION_EXPIRATION_DATE)
    private Date pendingTransactionExpirationDate;

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public List<String> getReservationIds() {
        return reservationIds;
    }

    public void setReservationIds(List<String> reservationIds) {
        this.reservationIds = reservationIds;
    }

    public String getCheckpointId() {
        return checkpointId;
    }

    public void setCheckpointId(String checkpointId) {
        this.checkpointId = checkpointId;
    }

    public Date getPendingTransactionExpirationDate() {
        return pendingTransactionExpirationDate;
    }

    public void setPendingTransactionExpirationDate(Date pendingTransactionExpirationDate) {
        this.pendingTransactionExpirationDate = pendingTransactionExpirationDate;
    }

    public static PartnerTransactionPayload createFromReservationIds(String... ids) {
        PartnerTransactionPayload payload = new PartnerTransactionPayload();
        if (ids.length == 1) {
            payload.setReservationId(ids[0]);
        } else {
            payload.setReservationIds(Lists.newArrayList(ids));
        }
        return payload;
    }
}
