package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public interface BerlinGroupAccountEntity {

    Optional<TransactionalAccount> toTinkAccount();

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
