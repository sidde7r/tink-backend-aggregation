package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
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

    public LoanAccount toLoanAccount(DanskeBankConfiguration configuration) {
        return LoanAccount.builder(accountNoInt, ExactCurrencyAmount.of(balance, currency))
                .setAccountNumber(accountNoExt)
                .setName(accountName)
                .setBankIdentifier(accountNoInt)
                .canMakeDomesticTransfer(configuration.canMakeDomesticTransfer(accountProduct))
                .canReceiveDomesticTransfer(
                        configuration.canReceiveDomesticTransfer(accountProduct))
                .canPlaceFunds(configuration.canPlaceFunds(accountProduct))
                .canWithdrawFunds(configuration.canWithdrawFunds(accountProduct))
                .build();
    }

    public CreditCardAccount toCreditCardAccount(DanskeBankConfiguration configuration) {
        return CreditCardAccount.builder(
                        accountNoInt,
                        ExactCurrencyAmount.of(balance, currency),
                        calculateAvailableCredit())
                .setAccountNumber(accountNoExt)
                .setName(accountName)
                .setBankIdentifier(accountNoInt)
                .canMakeDomesticTransfer(configuration.canMakeDomesticTransfer(accountProduct))
                .canReceiveDomesticTransfer(
                        configuration.canReceiveDomesticTransfer(accountProduct))
                .canPlaceFunds(configuration.canPlaceFunds(accountProduct))
                .canWithdrawFunds(configuration.canWithdrawFunds(accountProduct))
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
                // checking accounts are heaving by default the following 4 capabilities
                // but you can confirm that easily the same way as done for other account types
                .canMakeDomesticTransfer(AccountCapabilities.Answer.YES)
                .canReceiveDomesticTransfer(AccountCapabilities.Answer.YES)
                .canPlaceFunds(AccountCapabilities.Answer.YES)
                .canWithdrawFunds(AccountCapabilities.Answer.YES)
                .addAccountFlag(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                .build();
    }

    public TransactionalAccount toSavingsAccount(DanskeBankConfiguration configuration) {
        return SavingsAccount.builder(
                        AccountTypes.SAVINGS,
                        accountNoInt,
                        ExactCurrencyAmount.of(balance, currency))
                .setAccountNumber(accountNoExt)
                .setName(accountName)
                .setBankIdentifier(accountNoInt)
                .canMakeDomesticTransfer(configuration.canMakeDomesticTransfer(accountProduct))
                .canReceiveDomesticTransfer(
                        configuration.canReceiveDomesticTransfer(accountProduct))
                .canPlaceFunds(configuration.canPlaceFunds(accountProduct))
                .canWithdrawFunds(configuration.canWithdrawFunds(accountProduct))
                .build();
    }

    private ExactCurrencyAmount calculateAvailableCredit() {
        return ExactCurrencyAmount.of(Math.max(balanceAvailable - balance, 0.0), currency);
    }
}
