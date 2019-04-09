package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.entities;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {
    private String accountId;
    private String accountName;
    private double balance;
    private String balanceTxt;
    private String dateLastRecord;
    private double maximum;
    private String maximumTxt;
    private String currency;
    private int accountAuthCode;
    private boolean isStdFraKonto;
    private boolean hasExpenditureOverview;

    public String getAccountId() {
        return accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public double getBalance() {
        return balance;
    }

    public String getBalanceTxt() {
        return balanceTxt;
    }

    public String getDateLastRecord() {
        return dateLastRecord;
    }

    public double getMaximum() {
        return maximum;
    }

    public String getMaximumTxt() {
        return maximumTxt;
    }

    public String getCurrency() {
        return currency;
    }

    public int getAccountAuthCode() {
        return accountAuthCode;
    }

    public boolean getStdFraKonto() {
        return isStdFraKonto;
    }

    public boolean getHasExpenditureOverview() {
        return hasExpenditureOverview;
    }

    public Amount getTinkBalance() {
        return new Amount(currency, balance);
    }

    public TransactionalAccount toTinkTransactionalAccount(AccountDetailsResponse details) {
        // Should not be able to throw an exception here.
        AccountTypes accountType =
                details.getTinkAccountType()
                        .orElseThrow(() -> new IllegalStateException("Unknown account type"));

        return TransactionalAccount.builder(accountType, accountId, getTinkBalance())
                .setAccountNumber(accountId)
                .setBankIdentifier(accountId)
                .setName(accountName)
                .setHolderName(new HolderName(details.getAccountHolder()))
                .build();
    }
}
