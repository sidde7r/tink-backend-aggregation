package se.tink.backend.aggregation.agents.creditcards.supremecard.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionTypes;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {
    private String transactionDate;
    private String registrationDate;
    private String nameOfTransaction;
    private String amount;
    private String originalAmount;
    private String exchangeRate;
    private String sellerName;
    private String country;
    private String city;
    private String originalCurrency;

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getNameOfTransaction() {
        return nameOfTransaction;
    }

    public void setNameOfTransaction(String nameOfTransaction) {
        this.nameOfTransaction = nameOfTransaction;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(String originalAmount) {
        this.originalAmount = originalAmount;
    }

    public String getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(String exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getOriginalCurrency() {
        return originalCurrency;
    }

    public void setOriginalCurrency(String originalCurrency) {
        this.originalCurrency = originalCurrency;
    }

    private String constructDescription() {
        return !Strings.isNullOrEmpty(sellerName) ? sellerName : nameOfTransaction;
    }

    public Transaction toTransaction() {
        Transaction transaction = new Transaction();

        transaction.setDescription(constructDescription());
        transaction.setDate(AgentParsingUtils.parseDate(transactionDate, true));
        transaction.setAmount(AgentParsingUtils.parseAmount(amount));
        transaction.setType(TransactionTypes.CREDIT_CARD);

        return transaction;
    }
}
