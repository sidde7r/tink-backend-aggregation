package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Date;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class TransactionEntity {
    private String amount;
    private Boolean booked;
    private String currency;
    private String title;

    @JsonProperty("to_account_number")
    private String toAccountNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("booking_date")
    private Date bookingDate;

    public boolean isBooked() {
        return booked;
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(ExactCurrencyAmount.of(amount, currency))
                .setDate(bookingDate)
                .setDescription(title)
                .setPending(!booked)
                .setPayload(
                        TransactionPayloadTypes.DETAILS,
                        SerializationUtils.serializeToString(getTransactionDetails()))
                .build();
    }

    @JsonIgnore
    public TransactionDetails getTransactionDetails() {
        return new TransactionDetails(StringUtils.EMPTY, Strings.nullToEmpty(toAccountNumber));
    }
}
