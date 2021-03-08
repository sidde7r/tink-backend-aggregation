package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.engagements;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.utils.IkanoParser;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.utils.CreditCardMasker;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardEntity {
    private String cardName;
    private String cardNumber;
    private String agreementNumber;
    private double creditLimit;
    private double availableCredit;
    private String cardType;
    private String customerName;
    private List<TransactionEntity> transactions;

    public Account toTinkAccount() {
        Account tinkAccount = new Account();

        tinkAccount.setType(AccountTypes.CREDIT_CARD);
        tinkAccount.setName(cardName);
        tinkAccount.setBankId(agreementNumber);
        tinkAccount.setAvailableCredit(availableCredit);
        tinkAccount.setBalance(this.calculateBalance());
        if (!Strings.isNullOrEmpty(cardNumber)) {
            tinkAccount.setAccountNumber(CreditCardMasker.maskCardNumber(cardNumber));
        } else {
            tinkAccount.setAccountNumber(agreementNumber);
        }
        tinkAccount.setHolderName(customerName);

        return tinkAccount;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions != null ? transactions : Lists.<TransactionEntity>newArrayList();
    }

    public List<Transaction> getTinkTransactions() {
        List<Transaction> tinkTransactions = Lists.newArrayList();

        for (TransactionEntity transaction : getTransactions()) {
            tinkTransactions.add(transaction.toTinkTransaction());
        }

        return tinkTransactions;
    }

    public String getCustomerName() {
        return customerName;
    }

    public boolean isRelatedTo(Account account) {
        return agreementNumber.equals(account.getBankId());
    }

    private double calculateBalance() {
        return availableCredit - creditLimit;
    }

    @JsonProperty("_CustomerName")
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @JsonProperty("_ProductCode")
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    @JsonProperty("_ProductName")
    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    @JsonProperty("_CardNumber")
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    @JsonProperty("_AgreementNumber")
    public void setAgreementNumber(String agreementNumber) {
        this.agreementNumber = agreementNumber;
    }

    @JsonProperty("_Limit")
    public void setCreditLimit(String creditLimit) {
        this.creditLimit = IkanoParser.stringToDouble(creditLimit);
    }

    @JsonProperty("_OpenToBuy")
    public void setAvailableCredit(String balance) {
        this.availableCredit = IkanoParser.stringToDouble(balance);
    }

    @JsonProperty("_transactions")
    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }

    public boolean isOfType(CardType cardType) {
        return cardType.hasIdentifier(this.cardType);
    }
}
