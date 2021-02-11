package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.creditcards.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Map;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1AmountUtils;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Data
public class CreditCardTransactionEntity {
    private Date postingDate;
    private Date transactionDate;
    private String transactionText;
    private String transactionCurrency;
    private String transactionAmount;
    private String transactionAmountFraction;
    private String billingAmount;
    private String billingAmountFraction;
    private String cashbackAmountCurrency;

    @JsonProperty("_links")
    private Map<String, LinkEntity> links;

    @JsonIgnore
    public CreditCardTransaction toTinkTransaction() {
        return CreditCardTransaction.builder()
                .setAmount(getAmount())
                .setDescription(transactionText)
                .setDate(transactionDate != null ? transactionDate : postingDate)
                .setPending(postingDate == null)
                .build();
    }

    private ExactCurrencyAmount getAmount() {
        if (StringUtils.isBlank(billingAmount)) {
            return ExactCurrencyAmount.of(
                            Sparebank1AmountUtils.constructDouble(
                                    transactionAmount, transactionAmountFraction),
                            transactionCurrency)
                    .negate();
        }
        return ExactCurrencyAmount.of(
                Sparebank1AmountUtils.constructDouble(billingAmount, billingAmountFraction),
                cashbackAmountCurrency);
    }
}
