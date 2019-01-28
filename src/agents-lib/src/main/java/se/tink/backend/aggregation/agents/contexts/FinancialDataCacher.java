package se.tink.backend.aggregation.agents.contexts;

import java.util.List;
import javax.annotation.Nonnull;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Transaction;

public interface FinancialDataCacher {

    @Deprecated
        // Use cacheTransactions instead
    Account updateTransactions(Account account, List<Transaction> transactions);

    /**
     * @param accountUniqueId The client-side unique identifier for the account associated with the
     *                        transactions. Same as nxgen Account.getUniqueIdentifier and rpc Account.getBankId.
     * @param transactions    Transactions to be stored in this context
     */
    void cacheTransactions(@Nonnull String accountUniqueId, List<Transaction> transactions);

    /**
     * Caches {@code account} and the associated {@code accountFeatures}, making {@code accountFeatures} retrievable
     * via {@code AgentContext::getAccountFeatures} after this method has been executed.
     *
     * @param account         An account
     * @param accountFeatures A set of account features associated with said account
     */
    void cacheAccount(Account account, AccountFeatures accountFeatures);

    default void cacheAccount(Account account) {
        cacheAccount(account, AccountFeatures.createEmpty());
    }

    default void cacheAccounts(Iterable<Account> accounts) {
        for (Account account : accounts) {
            cacheAccount(account);
        }
    }
}
