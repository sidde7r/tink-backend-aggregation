package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.entity.transaction;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BookedEntity {

    private String bookingDate;
    private String creditorName;
    private String entryReference;
    private List<ExchangeRateEntity> exchangeRate;
    private OriginalAmountEntity originalAmount;
    private TransactionAmountEntity transactionAmount;
    private String transactionText;
    private String valueDate;

    public String getBookingDate() {
        return bookingDate;
    }

    public TransactionAmountEntity getTransactionAmount() {
        return transactionAmount;
    }

    public String getTransactionText() {
        return transactionText;
    }
}
