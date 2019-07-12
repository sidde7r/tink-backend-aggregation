package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.TransactionStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.shared.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionEntity {
    private ExtensionsEntity extensions;
    private String subtype;
    private String status;

    @JsonProperty("gross")
    private AmountEntity amount;

    private CounterpartyEntity counterparty;
    private String id;

    @JsonProperty("time_created")
    private String timeCreated;

    @JsonProperty("activity_type")
    private String activityType;

    public Amount getAmount() {
        return amount.toTinkAmount();
    }

    public Date getDate() {
        try {
            return new SimpleDateFormat(Formats.TRANSACTION_DATE_FORMAT).parse(timeCreated);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getSubtype() {
        return subtype;
    }

    public boolean isPending() {
        return status.equals(TransactionStatus.PENDING);
    }
}
