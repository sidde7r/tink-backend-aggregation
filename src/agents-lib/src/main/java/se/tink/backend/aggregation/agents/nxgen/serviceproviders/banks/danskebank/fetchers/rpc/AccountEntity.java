package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.SavingsAccount;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class AccountEntity {
    @JsonIgnore
    private static final AggregationLogger log = new AggregationLogger(AccountEntity.class);
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
        Amount availableCredit = new Amount(currency, 0d); // Not possible to get available credit
        return CreditCardAccount.builder(accountNoInt, new Amount(currency, balance), availableCredit)
                .setAccountNumber(accountNoExt)
                .setName(accountName)
                .setBankIdentifier(accountNoInt)
                .build();
    }

    public CheckingAccount toCheckingAccount() {
        return CheckingAccount.builder(accountNoInt, new Amount(currency, balance))
                .setAccountNumber(accountNoExt)
                .setName(accountName)
                .setBankIdentifier(accountNoInt)
                .build();
    }

    public SavingsAccount toSavingsAccount() {
        return SavingsAccount.builder(accountNoInt, new Amount(currency, balance))
                .setAccountNumber(accountNoExt)
                .setName(accountName)
                .setBankIdentifier(accountNoInt)
                .build();
    }

    public void logLoanAccount() {
        log.infoExtraLong(SerializationUtils.serializeToString(this),
                DanskeBankConstants.LogTags.LOAN_ACCOUNT);
    }

    public void logTransactionalAccount() {
        log.infoExtraLong(SerializationUtils.serializeToString(this),
                DanskeBankConstants.LogTags.TRANSACTIONAL_ACCOUNT);
    }
}
