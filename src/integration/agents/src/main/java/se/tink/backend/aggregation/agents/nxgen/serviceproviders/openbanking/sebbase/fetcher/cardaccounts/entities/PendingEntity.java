package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCard;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class PendingEntity {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    private String cardAcceptorCity;
    private String cardAcceptorCountryCode;
    private String cardTransactionId;
    private ExchangeRateEntity exchangeRate;
    private Boolean invoiced;
    private String maskedPan;
    private String nameOnCard;
    private OriginalAmountEntity originalAmount;
    private String proprietaryBankTransactionCode;
    private TransactionAmountEntity transactionAmount;
    private String transactionDetails;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date valueDate;

    public Date getBookingDate() {
        return bookingDate;
    }

    public String getCardAcceptorCity() {
        return cardAcceptorCity;
    }

    public String getCardAcceptorCountryCode() {
        return cardAcceptorCountryCode;
    }

    public String getCardTransactionId() {
        return cardTransactionId;
    }

    public ExchangeRateEntity getExchangeRate() {
        return exchangeRate;
    }

    public Boolean getInvoiced() {
        return invoiced;
    }

    public String getMaskedPan() {
        return Strings.emptyToNull(maskedPan);
    }

    public String getNameOnCard() {
        return Strings.emptyToNull(nameOnCard);
    }

    public OriginalAmountEntity getOriginalAmount() {
        return originalAmount;
    }

    public String getProprietaryBankTransactionCode() {
        return proprietaryBankTransactionCode;
    }

    public TransactionAmountEntity getTransactionAmount() {
        return transactionAmount;
    }

    public String getTransactionDetails() {
        return transactionDetails;
    }

    public Date getValueDate() {
        return valueDate;
    }

    @JsonIgnore
    public CreditCardTransaction toTinkTransaction() {
        return CreditCardTransaction.builder()
                .setAmount(
                        new ExactCurrencyAmount(
                                transactionAmount.getAmount(), transactionAmount.getCurrency()))
                .setCreditCard(CreditCard.create(getNameOnCard(), getMaskedPan()))
                .setDate(getValueDate())
                .setDescription(getTransactionDetails())
                .setPending(Boolean.TRUE)
                .build();
    }
}
