package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardEntity {
    @JsonProperty("CardId")
    private String id;
    @JsonProperty("CardNo")
    private String number;
    @JsonProperty("CreditCardNo")
    private String creditNumber;
    @JsonProperty("DebitCardNo")
    private String debitNumber;
    @JsonProperty("CardStatus")
    private String status;
    @JsonProperty("CardName")
    private String cardName;
    @JsonProperty("ActionAllowed")
    private String actionAllowed;
    @JsonProperty("CardCategory")
    private String category;
    @JsonProperty("ExpireMonth")
    private String expireMonth;
    @JsonProperty("ExpireYear")
    private String expireYear;
    @JsonProperty("AccountName")
    private String accountName;
    @JsonProperty("AccountNo")
    private String accountNumber;
    @JsonProperty("ShowBalance")
    private String showBalance;
    @JsonProperty("Balance")
    private Double balance;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCreditNumber() {
        return creditNumber;
    }

    public void setCreditNumber(String creditNumber) {
        this.creditNumber = creditNumber;
    }

    public String getDebitNumber() {
        return debitNumber;
    }

    public void setDebitNumber(String debitNumber) {
        this.debitNumber = debitNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getActionAllowed() {
        return actionAllowed;
    }

    public void setActionAllowed(String actionAllowed) {
        this.actionAllowed = actionAllowed;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getExpireMonth() {
        return expireMonth;
    }

    public void setExpireMonth(String expireMonth) {
        this.expireMonth = expireMonth;
    }

    public String getExpireYear() {
        return expireYear;
    }

    public void setExpireYear(String expireYear) {
        this.expireYear = expireYear;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getShowBalance() {
        return showBalance;
    }

    public void setShowBalance(String showBalance) {
        this.showBalance = showBalance;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
}