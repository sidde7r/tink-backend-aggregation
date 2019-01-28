package se.tink.backend.aggregation.nxgen.core.account;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.pair.Pair;

public final class AccountTypePredicateMapper<KeyType> {

    // Would use a Map here but then we'd need to implement Predicate.equals() and maybe hashCode
    private final List<Pair<Predicate<KeyType>, AccountTypes>> predicates;
    private final AccountTypeMapperExecutor<KeyType> executor;

    public static class Builder<KT> {

        private final List<Pair<Predicate<KT>, AccountTypes>> predicates = new ArrayList<>();
        private AccountTypeMapperExecutor<KT> executor;

        private Builder() {}

        public AccountTypePredicateMapper<KT> build() {
            return new AccountTypePredicateMapper<>(this);
        }

        /**
         * A known key pattern, and the account type that a key matching it should be mapped to. The
         * mapping behavior is undefined if you specify two patterns with different associated
         * account types such that there exists a string that matches both.
         */
        public Builder<KT> fallbackValue(AccountTypes value, Predicate<KT> predicate) {
            predicates.add(new Pair<>(predicate, value));
            return this;
        }

        public Builder<KT> setExecutor(AccountTypeMapperExecutor<KT> executor) {
            this.executor = executor;
            return this;
        }
    }

    private AccountTypePredicateMapper(Builder<KeyType> builder) {
        super();

        predicates = builder.predicates;

        if (builder.executor != null) {
            executor = builder.executor;
        } else {
            executor = new LoggingAccountTypeMapperExecutor<>();
        }
    }

    public static <T> AccountTypePredicateMapper.Builder<T> builder() {
        return new Builder<>();
    }

    private boolean verify(KeyType key, AccountTypes value) {
        Optional<AccountTypes> translated = translate(key);
        return translated.isPresent() && translated.get() == value;
    }

    private boolean verify(KeyType key, Collection<AccountTypes> values) {
        Optional<AccountTypes> translated = translate(key);
        return translated.isPresent() && values.contains(translated.get());
    }

    public Optional<AccountTypes> translate(KeyType accountTypeKey) {

        List<Pair<Predicate<KeyType>, AccountTypes>> matchingRestriction =
                predicates
                        .stream()
                        .filter(pair -> pair.first.test(accountTypeKey))
                        .collect(Collectors.toList());

        // Eliminate duplicates
        Set<AccountTypes> accountTypeSet =
                matchingRestriction.stream().map(p -> p.second).collect(Collectors.toSet());

        if (accountTypeSet.size() >= 2) {
            AccountTypes anyAccountType = accountTypeSet.iterator().next(); // Pop any element
            executor.onAmbiguousPredicateMatch(accountTypeKey, matchingRestriction);
            return Optional.of(anyAccountType);
        } else if (accountTypeSet.size() == 1) {
            Pair<Predicate<KeyType>, AccountTypes> pair = matchingRestriction.iterator().next();
            Predicate<KeyType> matchingPredicate = pair.first;
            AccountTypes associatedAccountType = pair.second;
            executor.onUnambiguousPredicateMatch(
                    accountTypeKey, matchingPredicate, associatedAccountType);
            return Optional.of(associatedAccountType);
        }

        executor.onUnknownAccountType(accountTypeKey);
        return Optional.empty();
    }
}
