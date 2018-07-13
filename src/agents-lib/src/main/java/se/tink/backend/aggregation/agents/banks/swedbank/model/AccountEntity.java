package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import java.util.Optional;
import se.tink.backend.aggregation.utils.TrimmingStringDeserializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountEntity {
    protected String accountNumber;
    protected String availableAmount;
    protected String balance;
    protected String clearingNumber;
    protected String creditGranted;
    protected String currency;
    protected boolean currencyAccount;
    protected String fullyFormattedNumber;
    protected String id;
    protected boolean internalAccount;
    protected LinksEntity links;
    protected String name;
    protected String reservedAmount;
    protected String cardNumber;
    private DetailsEntity details;
    private String type;
    
    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAvailableAmount() {
        return availableAmount;
    }

    public String getBalance() {
        return balance;
    }

    public String getClearingNumber() {
        return clearingNumber;
    }

    public String getCreditGranted() {
        return creditGranted;
    }

    public String getCurrency() {
        return currency;
    }

    public String getFullyFormattedNumber() {
        return fullyFormattedNumber;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getReservedAmount() {
        return reservedAmount;
    }

    public boolean isCurrencyAccount() {
        return currencyAccount;
    }

    public boolean isInternalAccount() {
        return internalAccount;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    public void setAvailableAmount(String availableAmount) {
        this.availableAmount = availableAmount;
    }

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    public void setBalance(String balance) {
        this.balance = balance;
    }

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    public void setClearingNumber(String clearingNumber) {
        this.clearingNumber = clearingNumber;
    }

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    public void setCreditGranted(String creditGranted) {
        this.creditGranted = creditGranted;
    }

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setCurrencyAccount(boolean currencyAccount) {
        this.currencyAccount = currencyAccount;
    }

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    public void setFullyFormattedNumber(String fullyFormattedNumber) {
        this.fullyFormattedNumber = fullyFormattedNumber;
    }

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    public void setId(String id) {
        this.id = id;
    }

    public void setInternalAccount(boolean internalAccount) {
        this.internalAccount = internalAccount;
    }

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    public void setName(String name) {
        this.name = name;
    }

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    public void setReservedAmount(String reservedAmount) {
        this.reservedAmount = reservedAmount;
    }

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Optional<String> getTransactionsURI() {
        String URI = null;

        if (links != null) {
            LinkEntity next = links.getNext();

            if (next != null) {
                URI = next.getUri();
            }
        }

        return Optional.ofNullable(URI);
    }

    public LinksEntity getLinks() {
        return links;
    }

    public void setLinks(LinksEntity links) {
        this.links = links;
    }

    public DetailsEntity getDetails() {
        return details;
    }

    public void setDetails(DetailsEntity details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(AccountEntity.class)
                .add("id", id)
                .add("name", name)
                .add("currency", currency)
                .add("balance", balance)
                .add("accountNumber", accountNumber)
                .add("clearingNumber", clearingNumber)
                .add("fullyFormattedAccountNumber", fullyFormattedNumber)
                .add("links", links)
                .toString();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
