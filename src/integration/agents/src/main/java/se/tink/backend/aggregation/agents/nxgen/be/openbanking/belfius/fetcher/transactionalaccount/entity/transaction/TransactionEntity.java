package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TransactionEntity {

    private BigDecimal amount;
    private String currency;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date executionDate;

    private String remittanceInfo;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setDescription(remittanceInfo.replace("\n", " ").replace("\r", " "))
                .setAmount(ExactCurrencyAmount.of(amount, currency))
                .setDate(executionDate)
                .build();
    }
}
