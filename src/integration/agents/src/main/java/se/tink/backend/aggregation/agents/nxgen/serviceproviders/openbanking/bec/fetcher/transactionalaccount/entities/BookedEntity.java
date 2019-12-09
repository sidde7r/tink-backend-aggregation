package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.entities;

import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BookedEntity {

    private Date bookingDate;
    private CreditorAccountEntity creditorAccount;
    private String entryReference;
    private AmountEntity transactionAmount;
    private String transactionId;
    private Date valueDate;
    private Amount amount;

    public Transaction toTinkTransaction() {
        amount = getAmount();
        return Transaction.builder()
                .setAmount(ExactCurrencyAmount.of(amount.toBigDecimal(), amount.getCurrency()))
                .setDate(bookingDate)
                .setDescription(entryReference)
                .setPending(false)
                .build();
    }

    public Amount getAmount() {
        return transactionAmount.toAmount();
    }
}
