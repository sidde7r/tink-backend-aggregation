
package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import net.minidev.json.annotate.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCard;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.amount.Amount;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@JsonObject
public class Pending {


    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;
    
    private String cardAcceptorCity;
    
    private String cardAcceptorCountryCode;
    
    private String cardTransactionId;
    
    private ExchangeRate exchangeRate;
    
    private Boolean invoiced;
    
    private String maskedPan;
    
    private String nameOnCard;
    
    private OriginalAmount originalAmount;
    
    private String proprietaryBankTransactionCode;
    
    private TransactionAmount transactionAmount;
    
    private String transactionDetails;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date valueDate;

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getCardAcceptorCity() {
        return cardAcceptorCity;
    }

    public void setCardAcceptorCity(String cardAcceptorCity) {
        this.cardAcceptorCity = cardAcceptorCity;
    }

    public String getCardAcceptorCountryCode() {
        return cardAcceptorCountryCode;
    }

    public void setCardAcceptorCountryCode(String cardAcceptorCountryCode) {
        this.cardAcceptorCountryCode = cardAcceptorCountryCode;
    }

    public String getCardTransactionId() {
        return cardTransactionId;
    }

    public void setCardTransactionId(String cardTransactionId) {
        this.cardTransactionId = cardTransactionId;
    }

    public ExchangeRate getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(ExchangeRate exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public Boolean getInvoiced() {
        return invoiced;
    }

    public void setInvoiced(Boolean invoiced) {
        this.invoiced = invoiced;
    }

    public String getMaskedPan() {
        return maskedPan;
    }

    public void setMaskedPan(String maskedPan) {
        this.maskedPan = maskedPan;
    }

    public String getNameOnCard() {
        return nameOnCard;
    }

    public void setNameOnCard(String nameOnCard) {
        this.nameOnCard = nameOnCard;
    }

    public OriginalAmount getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(OriginalAmount originalAmount) {
        this.originalAmount = originalAmount;
    }

    public String getProprietaryBankTransactionCode() {
        return proprietaryBankTransactionCode;
    }

    public void setProprietaryBankTransactionCode(String proprietaryBankTransactionCode) {
        this.proprietaryBankTransactionCode = proprietaryBankTransactionCode;
    }

    public TransactionAmount getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(TransactionAmount transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getTransactionDetails() {
        return transactionDetails;
    }

    public void setTransactionDetails(String transactionDetails) {
        this.transactionDetails = transactionDetails;
    }

    public Date getValueDate() {
        return valueDate;
    }

    public void setValueDate(Date valueDate) {
        this.valueDate = valueDate;
    }

    @JsonIgnore
    public CreditCardTransaction toTinkTransaction(CreditCardAccount creditAccount) {
        CreditCardTransaction tr = CreditCardTransaction
                .builder()
                .setAmount(
                        new Amount(
                                transactionAmount.getCurrency(),
                                BigDecimal.valueOf(transactionAmount.getAmount())
                        )
                )
                .setCreditAccount(creditAccount)
                .setCreditCard(CreditCard.create(getNameOnCard(), getMaskedPan()))
                .setDate(getValueDate())
                .setDescription(getTransactionDetails())
                .setPending(Boolean.TRUE)
                .build();
        return tr;
    }
}
