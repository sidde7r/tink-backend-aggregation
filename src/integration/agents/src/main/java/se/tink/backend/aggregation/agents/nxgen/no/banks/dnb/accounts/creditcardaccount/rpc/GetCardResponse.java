package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class GetCardResponse {
    private String firstName;
    private String lastName;
    private double balanceAmount;
    private int creditLimit;
    private double availableAmount;
    private String contractId;
    private String accountNumber;
    private String cardNumber;
    private List<String> transferableAccounts;
    private boolean allowTransfers;
    private boolean allowSmsAlerts;
    private boolean allowPinOverNet;
    private boolean displayTransactions;
    private String transferStatusMessage;
    private String productId;
    private String productName;
    private String type;
    private String statusCode;
    private String statusDescription;
    private String coreSystem;
    private long accountCreated;
    private String userId;
    private boolean fakeSupplementaryCard;
    private String ssn;
    private boolean owner;
    private boolean mainCard;
    private boolean creditCard;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public double getBalanceAmount() {
        return balanceAmount;
    }

    public int getCreditLimit() {
        return creditLimit;
    }

    public double getAvailableAmount() {
        return availableAmount;
    }

    public String getContractId() {
        return contractId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public List<String> getTransferableAccounts() {
        return transferableAccounts;
    }

    public boolean isAllowTransfers() {
        return allowTransfers;
    }

    public boolean isAllowSmsAlerts() {
        return allowSmsAlerts;
    }

    public boolean isAllowPinOverNet() {
        return allowPinOverNet;
    }

    public boolean isDisplayTransactions() {
        return displayTransactions;
    }

    public String getTransferStatusMessage() {
        return transferStatusMessage;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getType() {
        return type;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public String getCoreSystem() {
        return coreSystem;
    }

    public long getAccountCreated() {
        return accountCreated;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isFakeSupplementaryCard() {
        return fakeSupplementaryCard;
    }

    public String getSsn() {
        return ssn;
    }

    public boolean isOwner() {
        return owner;
    }

    public boolean isMainCard() {
        return mainCard;
    }

    public boolean isCreditCard() {
        return creditCard;
    }

    public CreditCardAccount toTinkCard(String cardId) {
        return CreditCardAccount.builder(
                        StringUtils.hashAsStringSHA1(cardNumber),
                        getCardBalance(),
                        ExactCurrencyAmount.of(availableAmount, "NOK"))
                .setAccountNumber(cardNumber.replace(" ", ""))
                .setName(productName)
                .setBankIdentifier(cardId)
                .putInTemporaryStorage(
                        DnbConstants.CreditCard.TRANSACTION_TYPE, getTransactionType())
                .build();
    }

    @JsonIgnore
    private ExactCurrencyAmount getCardBalance() {
        if (mainCard) {
            return ExactCurrencyAmount.of(-balanceAmount, "NOK");
        } else {
            return ExactCurrencyAmount.of(0.0, "NOK");
        }
    }

    @JsonIgnore
    private String getTransactionType() {
        return mainCard ? DnbConstants.CreditCard.MAINHOLDER : DnbConstants.CreditCard.COHOLDER;
    }
}
