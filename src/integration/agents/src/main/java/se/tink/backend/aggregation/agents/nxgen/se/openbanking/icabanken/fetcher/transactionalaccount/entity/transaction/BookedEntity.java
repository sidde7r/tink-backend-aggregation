package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BookedEntity {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    private String creditorName;
    private String entryReference;
    private List<ExchangeRateEntity> exchangeRate;
    private OriginalAmountEntity originalAmount;
    private TransactionAmountEntity transactionAmount;
    private String transactionText;
    private String valueDate;

    public Date getBookingDate() {
        return bookingDate;
    }

    public TransactionAmountEntity getTransactionAmount() {
        return transactionAmount;
    }

    public String getTransactionText() {
        return transactionText;
    }
}
