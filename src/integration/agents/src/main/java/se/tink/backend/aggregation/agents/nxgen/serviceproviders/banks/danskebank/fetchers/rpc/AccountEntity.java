package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {

    private static final Logger log = LoggerFactory.getLogger(AccountEntity.class);

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
        return LoanAccount.builder(accountNoInt, new Amount(currency, balance))
                .setAccountNumber(accountNoExt)
                .setName(accountName)
                .setBankIdentifier(accountNoInt)
                .build();
    }

    public CreditCardAccount toCreditCardAccount() {

        return CreditCardAccount.builder(
                        accountNoInt, new Amount(currency, balance), calculateAvailableCredit())
                .setAccountNumber(accountNoExt)
                .setName(accountName)
                .setBankIdentifier(accountNoInt)
                .build();
    }

    public CheckingAccount toCheckingAccount() {
        log.info(
                "Account: apiIdentifier = {}, accountNumber = {}, accountProduct = {}",
                accountNoInt,
                accountNoExt,
                accountProduct);

        return CheckingAccount.builder(accountNoInt, new Amount(currency, balance))
                .setAvailableCredit(calculateAvailableCredit())
                .setAccountNumber(accountNoExt)
                .setName(accountName)
                .setBankIdentifier(accountNoInt)
                .addAccountFlag(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                .build();
    }

    public SavingsAccount toSavingsAccount() {
        return SavingsAccount.builder(accountNoInt, new Amount(currency, balance))
                .setAccountNumber(accountNoExt)
                .setName(accountName)
                .setBankIdentifier(accountNoInt)
                .build();
    }

    private Amount calculateAvailableCredit() {
        return new Amount(currency, Math.max(balanceAvailable - balance, 0.0));
    }
}
