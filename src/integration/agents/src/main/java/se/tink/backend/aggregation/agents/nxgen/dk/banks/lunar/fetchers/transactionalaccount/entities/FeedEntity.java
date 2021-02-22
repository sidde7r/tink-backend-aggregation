package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class FeedEntity extends BaseResponseEntity {
    private DepositEntity deposit;

    @JsonIgnore
    public boolean containsAmount() {
        return Optional.ofNullable(deposit).map(DepositEntity::getAmount).isPresent();
    }

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(ExactCurrencyAmount.of(deposit.getAmount(), deposit.getCurrency()))
                .setDate(new Date(sort))
                .setDescription(deposit.getText())
                .build();
    }

    @JsonObject
    @Data
    private static class DepositEntity {
        private BigDecimal amount;
        private String currency;
        private String id;
        private String text;
    }
}
