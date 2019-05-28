package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.AccountBaseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class AccountEntity extends AccountBaseEntity {

    @Override
    public TransactionalAccount toTinkAccount() {
        return super.toTinkAccount();
    }

    @Override
    protected TransactionalAccount toCheckingAccount() {
        return CheckingAccount.builder()
                .setUniqueIdentifier(iban)
                .setAccountNumber(iban)
                .setBalance(getBalance())
                .setAlias(name)
                .addAccountIdentifier(
                        AccountIdentifier.create(
                                AccountIdentifier.Type.IBAN, iban.substring(iban.length() - 18)))
                .addHolderName(name)
                .setApiIdentifier(resourceId)
                .putInTemporaryStorage(StorageKeys.TRANSACTIONS_URL, getTransactionLink())
                .build();
    }

    @Override
    protected TransactionalAccount toSavingsAccount() {
        return SavingsAccount.builder()
                .setUniqueIdentifier(iban)
                .setAccountNumber(iban)
                .setBalance(getBalance())
                .setAlias(name)
                .addAccountIdentifier(
                        AccountIdentifier.create(
                                AccountIdentifier.Type.IBAN, iban.substring(iban.length() - 18)))
                .addHolderName(name)
                .setApiIdentifier(resourceId)
                .putInTemporaryStorage(StorageKeys.TRANSACTIONS_URL, getTransactionLink())
                .build();
    }
}
