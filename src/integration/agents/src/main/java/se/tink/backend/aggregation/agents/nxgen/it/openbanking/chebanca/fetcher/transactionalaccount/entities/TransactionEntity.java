package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {
    private AmountEntity amountTransaction;
    private String extendedDescription;

    @JsonFormat(pattern = "yyyyMMdd'T'hh:mm:ss")
    private Date dateAccountingCurrency;

    private String shortDescription;

    @JsonFormat(pattern = "yyyyMMdd'T'hh:mm:ss")
    private Date dateLiquidationValue;

    private String codeDescription;

    @JsonIgnore
    public Transaction toBookedTransaction() {
        return toTinkTransaction(false);
    }

    @JsonIgnore
    public Transaction toPendingTransactions() {
        return toTinkTransaction(true);
    }

    @JsonIgnore
    private Transaction toTinkTransaction(boolean pending) {
        return Transaction.builder()
                .setPending(pending)
                .setAmount(amountTransaction.toAmount())
                .setDate(Optional.ofNullable(dateAccountingCurrency).orElse(dateLiquidationValue))
                .setDescription(shortDescription)
                .build();
    }
}
