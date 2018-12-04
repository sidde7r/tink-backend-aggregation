package se.tink.backend.aggregation.agents.brokers.avanza;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapperExecutor;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypePredicateMapper;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.common.utils.Pair;

public final class AvanzaV2AccountTypeMappers {
    private static final Logger LOGGER = LoggerFactory.getLogger(AvanzaV2AccountTypeMappers.class);

    private AccountTypeMapper accountTypeMapper;
    private AccountTypePredicateMapper<String> accountTypeFallbackMapper;

    private static Predicate<String> codeMatches(final String regex) {
        return t -> Pattern.compile(regex).matcher(t).matches();
    }

    public Optional<AccountTypes> inferAccountType(final String accountTypeKey) {
        final Optional<AccountTypes> accountType =
                Optional.of(getAccountTypeMapper().translate(accountTypeKey))
                        .orElse(getAccountTypeFallbackMapper().translate(accountTypeKey));

        // TODO refactor with isPresentOrElse when we are past Java 8
        if (!accountType.isPresent()) {
            LOGGER.warn(
                    "{} Could not infer account type from type \"{}\"; ignoring the account",
                    AvanzaV2Constants.LogTags.UNKNOWN_ACCOUNT_TYPE,
                    accountTypeKey);
        }

        return accountType;
    }

    private AccountTypeMapper getAccountTypeMapper() {
        if (accountTypeMapper == null) {
            accountTypeMapper =
                    AccountTypeMapper.builder()
                            .put(
                                    AccountTypes.INVESTMENT,
                                    "AktieFondkonto",
                                    "Investeringssparkonto",
                                    "Kapitalforsakring")
                            .put(AccountTypes.SAVINGS, "Sparkonto", "SparkontoPlus")
                            .put(AccountTypes.PENSION, "Tjanstepension")
                            .build();
        }
        return accountTypeMapper;
    }

    private AccountTypePredicateMapper<String> getAccountTypeFallbackMapper() {
        if (accountTypeFallbackMapper == null) {
            accountTypeFallbackMapper =
                    AccountTypePredicateMapper.<String>builder()
                            .setExecutor(new AvanzaV2AccountTypeMapperExecutor())
                            .fallbackValue(AccountTypes.PENSION, codeMatches("pension"))
                            .fallbackValue(AccountTypes.SAVINGS, codeMatches("sparkonto"))
                            .fallbackValue(AccountTypes.LOAN, codeMatches("kredit"))
                            .build();
        }
        return accountTypeFallbackMapper;
    }

    private class AvanzaV2AccountTypeMapperExecutor implements AccountTypeMapperExecutor<String> {
        private final Logger LOGGER =
                LoggerFactory.getLogger(AvanzaV2AccountTypeMapperExecutor.class);

        @Override
        public void onUnknownAccountType(final String accountTypeKey) {
            LOGGER.warn("Found unknown account type \"{}\"", accountTypeKey);
        }

        @Override
        public void onUnambiguousPredicateMatch(
                final String accountTypeKey,
                final Predicate<String> matchingPredicate,
                final AccountTypes accountType) {
            final String message =
                    "Account type key \"{}\" was not explicitly associated with an account type."
                            + " Setting it to {} since a fallback predicate is associated with it.";
            LOGGER.warn(message, accountTypeKey, accountType);
        }

        @Override
        public void onAmbiguousPredicateMatch(
                final String accountTypeKey,
                final List<Pair<Predicate<String>, AccountTypes>> matches) {
            final String message =
                    "Account type key \"{}\" was not explicitly associated with an account type,"
                            + " and matched multiple predicates associated with different account types.";
            LOGGER.warn(message, accountTypeKey);
        }
    }
}
