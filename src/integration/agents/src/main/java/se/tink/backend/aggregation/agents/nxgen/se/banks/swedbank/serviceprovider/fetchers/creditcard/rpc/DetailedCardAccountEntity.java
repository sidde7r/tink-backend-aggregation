package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.creditcard.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ExpenseControlEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DetailedCardAccountEntity {
    private String cardHolder;
    private String cardTermsDocumentLink;
    private boolean supplementary;
    private boolean blocked;
    private String expireDate;
    private boolean internetPurchases;
    private boolean internetPurchasesDisplayed;
    private String creditLimit;
    private String currentBalance;
    private String reservedAmount;
    private FeeInformationEntity feeInformation;
    private String name;
    private String id;
    private String availableAmount;
    private boolean availableForFavouriteAccount;
    private boolean availableForPriorityAccount;
    private String cardNumber;
    private ExpenseControlEntity expenseControl;
    private String creditCardProductId;

    public String getCardHolder() {
        return cardHolder;
    }

    public String getCardTermsDocumentLink() {
        return cardTermsDocumentLink;
    }

    public boolean isSupplementary() {
        return supplementary;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public boolean isInternetPurchases() {
        return internetPurchases;
    }

    public boolean isInternetPurchasesDisplayed() {
        return internetPurchasesDisplayed;
    }

    public String getCreditLimit() {
        return creditLimit;
    }

    public String getCurrentBalance() {
        return currentBalance;
    }

    public String getReservedAmount() {
        return reservedAmount;
    }

    public FeeInformationEntity getFeeInformation() {
        return feeInformation;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getAvailableAmount() {
        return availableAmount;
    }

    public boolean isAvailableForFavouriteAccount() {
        return availableForFavouriteAccount;
    }

    public boolean isAvailableForPriorityAccount() {
        return availableForPriorityAccount;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public ExpenseControlEntity getExpenseControl() {
        return expenseControl;
    }

    public String getCreditCardProductId() {
        return creditCardProductId;
    }
}
