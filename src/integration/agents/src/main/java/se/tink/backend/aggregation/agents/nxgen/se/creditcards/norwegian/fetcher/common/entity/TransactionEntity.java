package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.NorwegianConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {
    private int accountTransactionId;
    private String uniqueId;
    private int externalId;
    private BigDecimal amount;
    private String amountFormatted;
    private double currencyAmount;
    private String currencyCode;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date transactionDate;

    private String postingDate;
    private String postingDateFormatted;
    private String valueDate;
    private String transactionText;
    private String transactionTypeText;
    private String merchantName;
    private String merchantCity;
    private boolean isCurrencyTx;
    private double currencyRate;
    private String currencyRateText;
    private String currencyAmountText;
    private String merchantCountry;
    private String mccName;
    private String foreignAccountNo;
    private String foreignNameAddress;
    private String message;
    private String reference;
    private boolean isBooked;
    private boolean isPresentInOtherDispute;

    public CreditCardTransaction toTinkTransaction() {
        return CreditCardTransaction.builder()
                .setAmount(ExactCurrencyAmount.of(amount, NorwegianConstants.CURRENCY))
                .setDate(transactionDate)
                .setDescription(transactionText)
                .setPending(!isBooked)
                .build();
    }
}
