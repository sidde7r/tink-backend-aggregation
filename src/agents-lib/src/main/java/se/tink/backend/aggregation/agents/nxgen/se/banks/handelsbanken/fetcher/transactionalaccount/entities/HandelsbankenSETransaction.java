package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.regex.Matcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.entities.HandelsbankenRecipient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;

public class HandelsbankenSETransaction extends BaseResponse {

    private HandelsbankenAmount amount;
    private HandelsbankenRecipient recipient;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dueDate;

    public Transaction toTinkTransaction() {
        String description = recipient.getAdditionalInfo();
        boolean pending = false;

        Matcher matcher = HandelsbankenSEConstants.Fetcher.Transactions.PENDING_PATTERN.matcher(description);
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
}
