package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public interface BerlinGroupAccountEntity {

    boolean isCheckingOrSavingsType();

    TransactionalAccount toTinkAccount();

    boolean isCheckingType(final AccountTypes accountType);

    TransactionalAccount toCheckingAccount();

    TransactionalAccount toSavingsAccount();

    ExactCurrencyAmount getBalance();

    boolean doesMatchWithAccountCurrency(final BalanceBaseEntity balance);

    ExactCurrencyAmount getDefaultAmount();

    String getBalancesLink();

    String getTransactionLink();

    String getUniqueIdentifier();

    AccountIdentifier getIdentifier();

    String getAccountNumber();

    AccountIdentifier getAccountIdentifier();
}
