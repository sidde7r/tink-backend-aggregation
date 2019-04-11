package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionEntity {

    private String id;
    private String currency;
    private double amount;
    private String direction;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date created;

    private String narrative;
    private String source;
    private double balance;

    public Transaction toTinkTransaction() {

        return Transaction.builder()
                .setAmount(new Amount(currency, amount))
                .setDate(created)
                .setDescription(narrative)
                .build();
    }
}
