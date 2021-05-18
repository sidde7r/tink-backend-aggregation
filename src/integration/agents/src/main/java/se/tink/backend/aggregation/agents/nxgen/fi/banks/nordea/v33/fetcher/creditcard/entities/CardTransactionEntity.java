package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CardTransactionEntity {
    private String transactionId;
    private boolean booked;
    private double amount;
    private String currency;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date transactionDate;

    private String interestDate;
    private String title;
    private double balanceAfter;
    private String transactionType;

    public CreditCardTransaction toTinkCreditCardTransaction() {

        return CreditCardTransaction.builder()
                .setAmount(ExactCurrencyAmount.of(amount, currency))
                .setPending(!booked)
                .setDescription(StringUtils.isNotBlank(title) ? title : transactionType)
                .setDate(getDate())
                .build();
    }

    private Date getDate() {
        return booked ? bookingDate : transactionDate;
    }
}
