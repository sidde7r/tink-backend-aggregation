package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {
    private static final String MUTABLE_VALUE = "MUTABLE";

    @JsonProperty("id")
    private Integer id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;

    @JsonProperty("description")
    private String description;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("pending")
    private Boolean pending;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("mutability")
    private String mutability;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return (Transaction)
                Transaction.builder()
                        .setAmount(exactCurrencyAmount())
                        .setDescription(description)
                        .setDate(date)
                        .setPending(pending)
                        .setMutable(isMutable())
                        .build();
    }

    private Boolean isMutable() {
        if (Strings.isNullOrEmpty(mutability)) {
            return null;
        }
        return Objects.equals(MUTABLE_VALUE, mutability);
    }

    public ExactCurrencyAmount exactCurrencyAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }
}
