package se.tink.backend.aggregation.nxgen.core.account;

import java.util.List;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.pair.Pair;

public final class LoggingAccountTypeMapperExecutor<KeyType>
        implements AccountTypeMapperExecutor<KeyType> {
    private static final Logger logger =
            LoggerFactory.getLogger(LoggingAccountTypeMapperExecutor.class);

    @Override
    public void onUnknownAccountType(KeyType accountTypeString) {
        logger.warn("Unknown account type for key: {}", accountTypeString);
    }

    @Override
    public void onUnambiguousPredicateMatch(
            KeyType accountTypeString, Predicate<KeyType> matchingPredicate, AccountTypes accountType) {
        logger.warn(
                "Unspecified account type for key: \"{}\" -- using {} because the key satisfies a predicate associated with it",
                accountTypeString,
                accountType);
    }

    @Override
    public void onAmbiguousPredicateMatch(
            KeyType accountTypeKey, List<Pair<Predicate<KeyType>, AccountTypes>> matches) {
        AccountTypes anyAccountType = matches.iterator().next().second;
        logger.error(
                "Account type string: \"{}\" matched multiple predicates with different associated account types: {} -- using {}",
                accountTypeKey,
                matches,
                anyAccountType);
    }
}
