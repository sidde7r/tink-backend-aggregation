package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62DateDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {
    private String suppIndex;
    private String type;
    // TODO: How do we specify locale, as it's obtained at runtime
    @JsonProperty("chargeDate")
    @JsonFormat(pattern = "yyyyMMdd")
    @JsonDeserialize(using = AmericanExpressV62DateDeserializer.class)
    private Date date;
    private AmountEntity amount;
    private List<String> description;

    public String getSuppIndex() {
        return suppIndex;
    }

    public Transaction toTransaction(AmericanExpressV62Configuration config, boolean isPending) {
        return Transaction.builder()
                .setAmount(config.toAmount(amount.getRawValue()))
                .setDate(getDate())
                .setPending(isPending)
                .setDescription(description.get(0))
                .build();
    }

    // Just a standard defensive copy
    public Date getDate() {
        return new Date(date.getTime());
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
}
