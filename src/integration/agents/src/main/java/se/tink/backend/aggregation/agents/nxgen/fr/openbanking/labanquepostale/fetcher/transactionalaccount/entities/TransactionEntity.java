package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {
    private AmountEntity transactionAmount;
    private String creditDebitIndicator;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    private List<String> remittanceInformation;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setPending(!isBooked())
                .setAmount(transactionAmount.toAmount(creditDebitIndicator))
                .setDate(bookingDate)
                .setDescription(remittanceInformation.get(0))
                .build();
    }

    public boolean isBooked() {
        return status.equals("BOOK");
    }
}
