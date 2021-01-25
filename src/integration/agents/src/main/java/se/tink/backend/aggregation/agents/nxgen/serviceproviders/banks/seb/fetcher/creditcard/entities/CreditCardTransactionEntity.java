package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CreditCardTransactionEntity {
    @JsonProperty("ADV_AMT")
    private int rawAmount;

    @JsonProperty("ADV_AMT_CURR_CODE")
    private String currency;

    @JsonProperty("AMT_DEC_QUANT")
    private int amountDecDigits;

    @JsonProperty("CARD_NO")
    private String cardNumber;

    @JsonProperty("CONV_RATE_QUANT")
    private double exchangeRate;

    @JsonProperty("CURR_MARKUP_PERC")
    private double exchangeRateMargin;

    @JsonProperty("ORIG_AMT")
    private int rawOriginalAmount;

    @JsonProperty("ORIG_AMT_CURR_CODE")
    private String originalCurrency;

    @JsonProperty("ORIG_AMT_DATE")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date originalAmountDate;

    @JsonProperty("ORIG_AMT_DEC_QUANT")
    private int originalAmountDecDigits;

    @JsonProperty("POSTING_DATE")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date postingDate;

    @JsonProperty("SE_NAME")
    private String description;

    @JsonProperty("TRANS_ID")
    private String transactionId;

    public Optional<Transaction> toTinkTransaction(
            CreditCardAccount creditCardAccount, boolean isPending) {

        Optional<Date> date = getTransactionDate();

        if (!date.isPresent()) {
            return Optional.empty();
        }

        if (isBillPayment()) {
            Transaction.Builder builder = Transaction.builder();
            setCommonFields(builder, date.get(), isPending);
            return Optional.of(builder.setType(TransactionTypes.DEFAULT).build());
        } else {
            CreditCardTransaction.Builder builder = CreditCardTransaction.builder();
            setCommonFields(builder, date.get(), isPending);
            return Optional.of(builder.setCreditAccount(creditCardAccount).build());
        }
    }

    private boolean isBillPayment() {
        return Strings.isNullOrEmpty(cardNumber);
    }

    private void setCommonFields(Transaction.Builder builder, Date date, boolean isPending) {
        builder.setAmount(getAmount())
                .setPending(isPending)
                .setDate(date)
                .setDescription(description)
                .setPayload(TransactionPayloadTypes.LOCAL_CURRENCY, originalCurrency)
                .setPayload(TransactionPayloadTypes.AMOUNT_IN_LOCAL_CURRENCY, getOriginalAmount())
                .setPayload(TransactionPayloadTypes.EXCHANGE_RATE, String.valueOf(exchangeRate))
                .setPayload(TransactionPayloadTypes.SUB_ACCOUNT, cardNumber);
    }

    private ExactCurrencyAmount getAmount() {
        return new ExactCurrencyAmount(
                BigDecimal.valueOf(rawAmount).movePointLeft(amountDecDigits).negate(), currency);
    }

    private Optional<Date> getTransactionDate() {
        if (originalAmountDate != null) {
            return Optional.of(originalAmountDate);
        }
        if (postingDate != null) {
            return Optional.of(postingDate);
        }
        return Optional.empty();
    }

    private String getOriginalAmount() {
        return BigDecimal.valueOf(rawOriginalAmount)
                .movePointLeft(originalAmountDecDigits)
                .negate()
                .toString();
    }
}
