package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.Date;
import java.util.regex.Matcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.DateUtils;

@JsonObject
public class HandelsbankenSETransaction extends BaseResponse {

    private HandelsbankenAmount amount;
    private HandelsbankenRecipient recipient;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dueDate;

    public Transaction toTinkTransaction() {
        String description = recipient.getAdditionalInfo();
        boolean pending = false;

        Matcher matcher =
                HandelsbankenSEConstants.Transactions.PENDING_PATTERN.matcher(description);
        if (matcher.find()) {
            description = matcher.replaceFirst("");
            pending = true;
        }

        return Transaction.builder()
                .setAmount(Amount.inSEK(amount.asDouble()))
                .setDate(dueDate)
                .setDescription(description)
                .setPending(pending)
                .build();
    }

    @JsonIgnore
    public LocalDate dueDateAsLocalDate() {
        if (dueDate == null) {
            return null;
        }

        return DateUtils.toJavaTimeLocalDate(dueDate);
    }

    @JsonIgnore
    public Amount positiveAmount() {
        if (amount == null) {
            return null;
        }
        Amount positiveAmount = Amount.inSEK(amount.asDouble());

        if (!positiveAmount.isPositive()) {
            return positiveAmount.negate();
        }

        return positiveAmount;
    }
}
