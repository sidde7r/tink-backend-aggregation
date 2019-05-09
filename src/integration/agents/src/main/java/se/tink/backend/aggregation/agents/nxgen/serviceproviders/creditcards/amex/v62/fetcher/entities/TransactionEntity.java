package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {
    private String suppIndex;
    private String type;
    private ChargeDate chargeDate;

    private AmountEntity amount;
    private List<String> description;

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
    public Transaction toTransaction(AmericanExpressV62Configuration config, boolean isPending) {
        return Transaction.builder()
                .setAmount(config.toAmount(amount.getRawValue()))
                .setDate(getDate())
                .setPending(isPending)
                .setDescription(getDescriptionString())
                .build();
    }

    @JsonIgnore
    public String getDescriptionString() {
        return description.stream().collect(Collectors.joining("\n"));
    }
}
