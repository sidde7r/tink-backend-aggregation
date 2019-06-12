package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

public interface BerlinGroupAccount {

    boolean isCheckingOrSavingsType();

    TransactionalAccount toTinkAccount();

    boolean isCheckingType(final AccountTypes accountType);

    TransactionalAccount toCheckingAccount();

    TransactionalAccount toSavingsAccount();

    Amount getBalance();

    boolean doesMatchWithAccountCurrency(final BalanceBaseEntity balance);

    Amount getDefaultAmount();

    String getBalancesLink();

    String getTransactionLink();

    String getUniqueIdentifier();

    String getAccountNumber();

    AccountIdentifier getAccountIdentifier();
}
