package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.entities.transactions;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsItemEntity {

    private String remittanceInformationUnstructured;
    private AmountEntity transactionAmount;
    private Date bookingDate;
    private Date transactionDate;

    public Transaction toTinkTransactions(boolean pending) {
        return Transaction.builder()
                .setAmount(transactionAmount.toAmount())
                .setDate(bookingDate != null ? bookingDate : transactionDate)
                .setDescription(remittanceInformationUnstructured)
                .setPending(pending)
                .build();
    }
}
