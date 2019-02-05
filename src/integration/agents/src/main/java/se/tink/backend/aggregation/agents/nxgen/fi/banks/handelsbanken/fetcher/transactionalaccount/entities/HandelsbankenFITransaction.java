package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

public class HandelsbankenFITransaction extends BaseResponse {

    private HandelsbankenAmount amount;
    private String reference;
    @JsonFormat(pattern = "dd.MM.yyyy")
    private Date bookingDate;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(Amount.inEUR(amount.asDouble()))
                .setDate(bookingDate)
                .setDescription(reference)
                .build();
    }
}
