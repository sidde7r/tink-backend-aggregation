package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.fetcher.transactionalaccount.entities;

import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants.BookingStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {
    private Date bookingDate;
    private String creditDebitIndicator;
    private String entryReference;
    private List<String> remittanceInformation;
    private String resourcesId;
    private String status;
    private AmountEntity transactionAmount;
    private Date transactionDate;
    private String valueDate;

    public Transaction toTinkTransaction() {
        final boolean pending = status.equalsIgnoreCase(BookingStatus.PENDING);

        return Transaction.builder()
                .setPending(pending)
                .setAmount(transactionAmount.toAmount())
                .setDate(bookingDate)
                .setDescription(String.join(", ", remittanceInformation))
                .build();
    }
}
