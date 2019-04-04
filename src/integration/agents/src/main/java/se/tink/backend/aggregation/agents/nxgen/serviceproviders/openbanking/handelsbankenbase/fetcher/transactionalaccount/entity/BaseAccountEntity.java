package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.entity;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseConstants.StorageKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public abstract class BaseAccountEntity {

    protected String accountId;
    protected String iban;
    protected String bban;
    protected String currency;
    protected String accountType;
    protected String bic;
    protected String clearingNumber;
    protected Double creditLimit;
    protected String name;
    protected String ownerName;

    public String getAccountId() {
        return accountId;
    }

    public String getAccountType() {
        return accountType;
    }

    public abstract TransactionalAccount toTinkAccount(BalanceEntity balance);

    public CheckingAccount createCheckingAccount(BalanceEntity balance) {
        return CheckingAccount.builder()
                .setUniqueIdentifier(iban)
                .setAccountNumber(iban)
                .setBalance(
                        new Amount(
                                balance.getAmount().getCurrency(),
                                balance.getAmount().getContent()))
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .addHolderName(ownerName)
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, accountId)
                .build();
    }

    public SavingsAccount createSavingsAccount(BalanceEntity balance) {
        return SavingsAccount.builder()
                .setUniqueIdentifier(iban)
                .setAccountNumber(iban)
                .setBalance(
                        new Amount(
                                balance.getAmount().getCurrency(),
                                balance.getAmount().getContent()))
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .addHolderName(ownerName)
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, accountId)
                .build();
    }
}
