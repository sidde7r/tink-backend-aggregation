package se.tink.backend.aggregation.agents.creditcards.supremecard.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Objects;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {
    private String amount;
    private String city;
    private String country;
    private String exchangeRate;
    private String nameOfTransaction;
    private String originalAmount;
    private String originalCurrency;
    private String registrationDate;
    private String sellerName;
    private String transactionDate;

    public String getAmount() {
        return amount;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getExchangeRate() {
        return exchangeRate;
    }

    public String getNameOfTransaction() {
        return nameOfTransaction;
    }

    public String getOriginalAmount() {
        return originalAmount;
    }

    public String getOriginalCurrency() {
        return originalCurrency;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public String getSellerName() {
        return sellerName;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setExchangeRate(String exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public void setNameOfTransaction(String nameOfTransaction) {
        this.nameOfTransaction = nameOfTransaction;
    }

    public void setOriginalAmount(String originalAmount) {
        this.originalAmount = originalAmount;
    }

    public void setOriginalCurrency(String originalCurrency) {
        this.originalCurrency = originalCurrency;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Transaction toTransaction() {
        Transaction transaction = new Transaction();

        transaction.setDescription(this.sellerName);
        transaction.setDate(AgentParsingUtils.parseDate(this.transactionDate, true));
        transaction.setAmount(AgentParsingUtils.parseAmount(this.amount));

        String nameOfTransaction = this.nameOfTransaction;

        if (Objects.equal("KÖP", nameOfTransaction)) {
            transaction.setType(TransactionTypes.CREDIT_CARD);
        } else if (StringUtils.trimToNull(this.sellerName) == null) {
            transaction.setDescription(nameOfTransaction);
        }

        return transaction;
    }
}
