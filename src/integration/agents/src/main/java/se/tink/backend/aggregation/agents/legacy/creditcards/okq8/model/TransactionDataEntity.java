package se.tink.backend.aggregation.agents.creditcards.okq8.model;

import com.google.common.base.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.google.common.base.Function;
import java.util.Date;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionDataEntity {
    
    private static String PENDING_TRANSACTION_DESCRIPTION = "Reserverat belopp";
    
    /**
     * Transformation function to be used by e.g. Iterables or List.transform(entities, TRANSFORM)
     */
    public static Function<TransactionDataEntity, Transaction> TO_TINK_TRANSACTION_TRANSFORM =
            TransactionDataEntity::toTinkTransaction;

    private String amount;
    @JsonProperty("card_number")
    private String cardNumber;
    private String date;
    private String description;
    @JsonProperty("shop_name")
    private String shopName;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    /**
     * String format of amount should be "4 780,00" according to API
     */
    public double getAmountAsDouble() {
        if (Strings.isNullOrEmpty(amount)) {
            throw new NullPointerException("Transaction amount not present. It should be.");
        }

        return StringUtils.parseAmount(amount);
    }

    /**
     * Format of the dates returned from okq8 is 2016-01-26 (without time specified), so we also flatten time to noon
     */
    public Date getDateAsDate() {
        if (Strings.isNullOrEmpty(date)) {
            throw new NullPointerException("Transaction date not present. It should be.");
        }

        Date parsedDateWithoutTimeSpecification = DateUtils.parseDate(date);

        return DateUtils.flattenTime(parsedDateWithoutTimeSpecification);
    }

    public Transaction toTinkTransaction() {
        Transaction transaction = new Transaction();

        double negativeAmount = -getAmountAsDouble();
        transaction.setAmount(negativeAmount);
        transaction.setDate(getDateAsDate());
        transaction.setDescription(getShopName());

        if (transaction.getAmount() < 0) {
            transaction.setType(TransactionTypes.CREDIT_CARD);
        }
        
        if (Objects.equal(getDescription(), PENDING_TRANSACTION_DESCRIPTION)) {
            transaction.setPending(true);
        }

        // Add this payload that can differ between different cards used for transactions
        transaction.setPayload(TransactionPayloadTypes.SUB_ACCOUNT, getCardNumber());

        return transaction;
    }
}
