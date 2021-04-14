package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.entities;

import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {

    private Date bookingDate;
    private AccountEntity creditorAccount;
    private AccountEntity debtorAccount;
    private String debtorName;
    private String entryReference;
    private String remittanceInformationUnstructured;
    private BalanceAmountEntity transactionAmount;
    private String transactionId;
    private String valueDate;

    public Transaction toBookingTransaction() {
        return Transaction.builder()
                .setDescription(remittanceInformationUnstructured)
                .setDate(bookingDate)
                .setAmount(transactionAmount.toAmount())
                .setPending(false)
                .build();
    }
}
