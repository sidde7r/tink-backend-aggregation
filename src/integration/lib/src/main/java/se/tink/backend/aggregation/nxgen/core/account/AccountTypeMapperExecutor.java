package se.tink.backend.aggregation.nxgen.core.account;

import java.util.List;
import java.util.function.Predicate;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.pair.Pair;

/** Side effects to execute when an event occurs in the AccountTypeMapper. */
public interface AccountTypeMapperExecutor<KeyType> {

    /**
     * Called whenever the account type mapper encounters a key it cannot recognize, even with a
     * fallback predicate.
     *
     * @param accountTypeKey The key associated with an account type
     */
    void onUnknownAccountType(KeyType accountTypeKey);

    /**
     * Called whenever the account type mapper encounters a key that is not explicitly mapped to an
     * account type, but which satisfies one or more fallback predicates which all map to the same
     * account type.
     *
     * @param accountTypeKey The key associated with an account type
     * @param matchingPredicate Any of the satisfied predicates associated with the account type
     * @param accountType The account type associated with the predicates
     */
    void onUnambiguousPredicateMatch(
            KeyType accountTypeKey, Predicate<KeyType> matchingPredicate, AccountTypes accountType);

    /**
     * Called whenever the account type mapper encounters a key that is not explicitly mapped to an
     * account type, but which satisfies two or more fallback predicates which do not all map to the
     * same account type (meaning, it is not clear what account type the key should fall back to).
     *
     * @param accountTypeKey The key associated with an account type
     * @param matches The predicates and their associated account types
     */
    void onAmbiguousPredicateMatch(
            KeyType accountTypeKey, List<Pair<Predicate<KeyType>, AccountTypes>> matches);
}
