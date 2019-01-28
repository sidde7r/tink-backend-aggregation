package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Preconditions;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardEntity {

    private String cardName;
    private String cardNumber;
    private String cardType;
    private double balance;
    private double cardLimit;
    private double cardAvailable;
    private double reservedAmount;

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getCardLimit() {
        return cardLimit;
    }

    public void setCardLimit(double cardLimit) {
        this.cardLimit = cardLimit;
    }

    public double getCardAvailable() {
        return cardAvailable;
    }

    public void setCardAvailable(double cardAvailable) {
        this.cardAvailable = cardAvailable;
    }

    public double getReservedAmount() {
        return reservedAmount;
    }

    public void setReservedAmount(double reservedAmount) {
        this.reservedAmount = reservedAmount;
    }

    public Account getAccount() {
        Account account = new Account();

        account.setBalance(-balance);
        account.setAccountNumber(cardNumber);
        account.setBankId(cardNumber);
        account.setName(cardName);
        account.setType(AccountTypes.CREDIT_CARD);

        Preconditions.checkState(
                Preconditions.checkNotNull(account.getBankId()).matches(
                        "[0-9]{4}|[0-9]{11}"),
                "Unexpected account.bankid '%s'. Reformatted?", account.getBankId());

        return account;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }
}
