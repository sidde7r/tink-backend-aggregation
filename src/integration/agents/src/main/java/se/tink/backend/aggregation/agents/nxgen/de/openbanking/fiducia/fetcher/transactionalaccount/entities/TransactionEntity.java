package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.entities;

import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {

    private Date bookingDate;
    private String proprietaryBankTransactionCode;
    private String remittanceInformationUnstructured;
    private AmountEntity transactionAmount;
    private String valueDate;

    public Transaction toBookedTransaction() {
        return Transaction.builder()
                .setAmount(transactionAmount.toAmount())
                .setDate(bookingDate)
                .setPending(false)
                .setDescription(remittanceInformationUnstructured)
                .build();
    }
}
