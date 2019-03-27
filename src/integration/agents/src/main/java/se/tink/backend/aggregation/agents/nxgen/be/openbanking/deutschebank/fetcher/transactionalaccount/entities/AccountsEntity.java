package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountsEntity {

    private String iban;

    private String currencyCode;

    private String accountType;

    private Number currentBalance;

    private String productDescription;

    public TransactionalAccount toTinkAccount(String owner) {
        return isCheckingAccount() ? toCheckingAccount(owner) : toSavingAccount(owner);
    }

    private TransactionalAccount toCheckingAccount(String owner) {
        return CheckingAccount.builder()
            .setUniqueIdentifier(iban)
            .setAccountNumber(iban)
            .setBalance(new Amount(currencyCode, currentBalance))
            .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.BE, iban))
            .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
            .addHolderName(owner)
            .setProductName(productDescription)
            .setApiIdentifier(iban)
            .putInTemporaryStorage(DeutscheBankConstants.StorageKeys.ACCOUNT_ID, iban)
            .build();
    }

    private TransactionalAccount toSavingAccount(String owner) {
        return SavingsAccount.builder()
            .setUniqueIdentifier(iban)
            .setAccountNumber(iban)
            .setBalance(new Amount(currencyCode, currentBalance))
            .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.BE, iban))
            .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
            .addHolderName(owner)
            .setProductName(productDescription)
            .setApiIdentifier(iban)
            .putInTemporaryStorage(DeutscheBankConstants.StorageKeys.ACCOUNT_ID, iban)
            .build();
    }

    public boolean isCheckingAccount() {
        return accountType.equalsIgnoreCase(DeutscheBankConstants.Accounts.CURRENT_ACCOUNT);
    }
}
