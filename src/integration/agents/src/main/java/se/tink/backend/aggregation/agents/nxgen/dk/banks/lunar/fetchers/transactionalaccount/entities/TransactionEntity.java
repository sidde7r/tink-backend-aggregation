package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@EqualsAndHashCode(callSuper = true)
@JsonObject
@Data
public class TransactionEntity extends BaseResponseEntity {
    private BigDecimal amount;
    private String currency;
    private String subtitle;
    private String title;
    private String transactionOriginGroupId;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(ExactCurrencyAmount.of(amount, currency))
                .setDate(new Date(sort))
                .setDescription(title)
                .build();
    }
}
