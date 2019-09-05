package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {
    private String suppIndex;
    private String type;
    private ChargeDate chargeDate;

    private AmountEntity amount;
    private List<String> description;

    @JsonIgnore private boolean isPending;

    public String getSuppIndex() {
        return suppIndex;
    }

    public void setSuppIndex(final String suppIndex) {
        this.suppIndex = suppIndex;
    }

    private Date getDate() {
        return chargeDate.toDate();
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public AmountEntity getAmount() {
        return amount;
    }

    public void setAmount(final AmountEntity amount) {
        this.amount = amount;
    }

    @JsonIgnore
    public boolean isPending() {
        return isPending;
    }

    @JsonIgnore
    public TransactionEntity setPending(boolean pending) {
        this.isPending = pending;
        return this;
    }

    @JsonIgnore
    public Transaction toTransaction(AmericanExpressV62Configuration configuration) {
        return Transaction.builder()
                .setAmount(
                        ExactCurrencyAmount.of(
                                BigDecimal.valueOf(amount.getRawValue()),
                                configuration.getCurrency()))
                .setDate(getDate())
                .setPending(isPending())
                .setDescription(getDescriptionString())
                .build();
    }

    @JsonIgnore
    private String getDescriptionString() {
        return description.stream().collect(Collectors.joining("\n"));
    }
}
