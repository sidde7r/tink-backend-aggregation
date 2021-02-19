package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionAccountEntity {
    private String availableAmount;
    private String reservedAmount;
    private QuickbalanceSubscriptionEntity quickbalanceSubscription;
    private boolean currencyAccount;
    private String creditGranted;
    private boolean internalAccount;
    private String name;
    private String id;
    private String currency;
    private String balance;
    private String accountNumber;
    private String clearingNumber;
    private String fullyFormattedNumber;
    private String iban;
    private boolean availableForFavouriteAccount;
    private boolean availableForPriorityAccount;
    private String originalName;
    private ExpenseControlEntity expenseControl;

    public String getAvailableAmount() {
        return availableAmount;
    }

    public String getReservedAmount() {
        return reservedAmount;
    }

    public QuickbalanceSubscriptionEntity getQuickbalanceSubscription() {
        return quickbalanceSubscription;
    }

    public boolean isCurrencyAccount() {
        return currencyAccount;
    }

    public String getCreditGranted() {
        return creditGranted;
    }

    public boolean isInternalAccount() {
        return internalAccount;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getCurrency() {
        return currency;
    }

    public String getBalance() {
        return balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getClearingNumber() {
        return clearingNumber;
    }

    public String getFullyFormattedNumber() {
        return fullyFormattedNumber;
    }

    public String getIban() {
        return iban;
    }

    public boolean isAvailableForFavouriteAccount() {
        return availableForFavouriteAccount;
    }

    public boolean isAvailableForPriorityAccount() {
        return availableForPriorityAccount;
    }

    public String getOriginalName() {
        return originalName;
    }

    public ExpenseControlEntity getExpenseControl() {
        return expenseControl;
    }
}
