package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class FeedEntity extends BaseResponseEntity {
    @JsonProperty("deposit")
    @JsonAlias("withdrawal")
    private TransferEntity transfer;

    @JsonIgnore
    public boolean containsAmount() {
        return Optional.ofNullable(transfer).map(TransferEntity::getAmount).isPresent();
    }

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(ExactCurrencyAmount.of(transfer.getAmount(), transfer.getCurrency()))
                .setDate(new Date(timestamp))
                .setDescription(transfer.getText())
                .build();
    }

    @JsonObject
    @Data
    private static class TransferEntity {
        private BigDecimal amount;
        private String currency;
        private String id;
        private String text;
    }
}
