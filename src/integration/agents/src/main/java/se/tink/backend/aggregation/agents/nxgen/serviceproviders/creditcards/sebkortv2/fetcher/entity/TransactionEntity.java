package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.card.Card;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {
    private long id;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss:SSSZ",
            locale = "sv_SE")
    private Date date;

    private String countryCode;
    private double amount;
    private String currencyCode;
    private double billingAmount;
    private String billingCurrencyCode;
    private String type;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss:SSSZ",
            locale = "sv_SE")
    private Date postingDate;

    private boolean cardBrandTransaction;
    private String specification;
    private boolean manageDocument;
    private boolean hasDocument;
    private boolean recurringPayment;
    private boolean ecomPurchase;
    private String invoiceReference;
    private boolean advised;
    private String city;
    private double currencyMarkupPercentage;
    private double exchangeRate;
    private int merchantCategoryCode;
    private int cardId;
    private String nameOnCard;
    private String maskedCardNumber;
    private long relatedReservationId;

    public long getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public double getBillingAmount() {
        return billingAmount;
    }

    public String getBillingCurrencyCode() {
        return billingCurrencyCode;
    }

    public String getType() {
        return type;
    }

    public Date getPostingDate() {
        return postingDate;
    }

    public boolean isCardBrandTransaction() {
        return cardBrandTransaction;
    }

    public String getSpecification() {
        return specification;
    }

    public boolean isManageDocument() {
        return manageDocument;
    }

    public boolean isHasDocument() {
        return hasDocument;
    }

    public boolean isRecurringPayment() {
        return recurringPayment;
    }

    public boolean isEcomPurchase() {
        return ecomPurchase;
    }

    public String getInvoiceReference() {
        return invoiceReference;
    }

    public boolean isAdvised() {
        return advised;
    }

    public String getCity() {
        return city;
    }

    public double getCurrencyMarkupPercentage() {
        return currencyMarkupPercentage;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public int getMerchantCategoryCode() {
        return merchantCategoryCode;
    }

    public int getCardId() {
        return cardId;
    }

    public String getNameOnCard() {
        return nameOnCard;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public long getRelatedReservationId() {
        return relatedReservationId;
    }

    @JsonIgnore
    public CreditCardTransaction toBookedTinkTransaction() {
        return toTinkTransaction(false);
    }

    @JsonIgnore
    public CreditCardTransaction toPendingTinkTransaction() {
        return toTinkTransaction(true);
    }

    @JsonIgnore
    private CreditCardTransaction toTinkTransaction(boolean isPending) {
        ExactCurrencyAmount amount = ExactCurrencyAmount.of(-billingAmount, billingCurrencyCode);

        return CreditCardTransaction.builder()
                .setCreditCard(Card.create(nameOnCard, maskedCardNumber))
                .setDate(date)
                .setDescription(specification)
                .setAmount(amount)
                .setPending(isPending)
                .build();
    }
}
