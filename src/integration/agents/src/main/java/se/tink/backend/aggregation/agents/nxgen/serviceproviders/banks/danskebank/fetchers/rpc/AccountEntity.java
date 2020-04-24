package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {

    private String showCategory;
    private int sortValue;
    private boolean isFixedTermDeposit;
    private boolean isInLimitGroup;
    private boolean isSavingGoalAccountProduct;
    private boolean isBreadcrumbAccountProduct;
    private boolean isLoanAccount;
    private String invIdOwner;
    private String mandateAccMk;
    private boolean showAvailable;
    private boolean accessToCredit;
    private boolean accessToDebit;
    private boolean accessToQuery;
    private String currency;
    private String cardType;
    private String accountType;
    private String accountName;
    private String accountProduct;
    private String accountRegNoExt;
    private String accountNoExt;
    private String accountNoInt;
    private String languageCode;
    private double balanceAvailable;
    private double balance;

    public String getShowCategory() {
        return showCategory;
    }

    public int getSortValue() {
        return sortValue;
    }

    public boolean isFixedTermDeposit() {
        return isFixedTermDeposit;
    }

    public boolean isInLimitGroup() {
        return isInLimitGroup;
    }

    public boolean isSavingGoalAccountProduct() {
        return isSavingGoalAccountProduct;
    }

    public boolean isBreadcrumbAccountProduct() {
        return isBreadcrumbAccountProduct;
    }

    public boolean isLoanAccount() {
        return isLoanAccount;
    }

    public String getInvIdOwner() {
        return invIdOwner;
    }

    public String getMandateAccMk() {
        return mandateAccMk;
    }

    public boolean isShowAvailable() {
        return showAvailable;
    }

    public boolean isAccessToCredit() {
        return accessToCredit;
    }

    public boolean isAccessToDebit() {
        return accessToDebit;
    }

    public boolean isAccessToQuery() {
        return accessToQuery;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCardType() {
        return cardType;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getAccountProduct() {
        return accountProduct;
    }

    public String getAccountRegNoExt() {
        return accountRegNoExt;
    }

    public String getAccountNoExt() {
        return accountNoExt;
    }

    public String getAccountNoInt() {
        return accountNoInt;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public double getBalanceAvailable() {
        return balanceAvailable;
    }

    public double getBalance() {
        return balance;
    }

    public LoanAccount toLoanAccount() {
        return LoanAccount.builder(accountNoInt, ExactCurrencyAmount.of(balance, currency))
                .setAccountNumber(accountNoExt)
                .setName(accountName)
                .setBankIdentifier(accountNoInt)
                .build();
    }

    public CreditCardAccount toCreditCardAccount() {
        return CreditCardAccount.builder(
                        accountNoInt,
                        ExactCurrencyAmount.of(balance, currency),
                        calculateAvailableCredit())
                .setAccountNumber(accountNoExt)
                .setName(accountName)
                .setBankIdentifier(accountNoInt)
                .build();
    }

    public TransactionalAccount toCheckingAccount() {
        return CheckingAccount.builder(
                        AccountTypes.CHECKING,
                        accountNoInt,
                        ExactCurrencyAmount.of(balance, currency))
                .setExactAvailableCredit(calculateAvailableCredit())
                .setAccountNumber(accountNoExt)
                .setName(accountName)
                .setBankIdentifier(accountNoInt)
                .addAccountFlag(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                .build();
    }

    public TransactionalAccount toSavingsAccount() {
        return SavingsAccount.builder(
                        AccountTypes.SAVINGS,
                        accountNoInt,
                        ExactCurrencyAmount.of(balance, currency))
                .setAccountNumber(accountNoExt)
                .setName(accountName)
                .setBankIdentifier(accountNoInt)
                .build();
    }

    private ExactCurrencyAmount calculateAvailableCredit() {
        return ExactCurrencyAmount.of(Math.max(balanceAvailable - balance, 0.0), currency);
    }
}
