package se.tink.backend.aggregation.agents.brokers.avanza;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapperExecutor;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypePredicateMapper;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.libraries.pair.Pair;
import static se.tink.backend.aggregation.agents.brokers.avanza.AvanzaV2Constants.AvanzaAccountTypes;
import static se.tink.backend.aggregation.agents.brokers.avanza.AvanzaV2Constants.AvanzaFallbackAccountTypes;

public final class AvanzaV2AccountTypeMappers {
    private static final AggregationLogger LOGGER =
            new AggregationLogger(AvanzaV2AccountTypeMappers.class);

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
                    String.format(
                            "%s Could not infer account type from type \"%s\"; ignoring the account",
                            AvanzaV2Constants.LogTags.UNKNOWN_ACCOUNT_TYPE,
                            accountTypeKey));
        }

        return accountType;
    }

    private AccountTypeMapper getAccountTypeMapper() {
        accountTypeMapper =
                Optional.ofNullable(accountTypeMapper)
                        .orElse(
                                AccountTypeMapper.builder()
                                        .put(
                                                AccountTypes.INVESTMENT,
                                                AvanzaAccountTypes.AKTIE_FONDKONTO,
                                                AvanzaAccountTypes.INVESTERINGSSPARKONTO,
                                                AvanzaAccountTypes.KAPITALFORSAKRING)
                                        .put(
                                                AccountTypes.SAVINGS,
                                                AvanzaAccountTypes.SPARKONTO,
                                                AvanzaAccountTypes.SPARKONTOPLUS)
                                        .put(
                                                AccountTypes.PENSION,
                                                AvanzaAccountTypes.TJANSTEPENSION,
                                                AvanzaAccountTypes.PENSIONSFORSAKRING,
                                                AvanzaAccountTypes.IPS)
                                        .build());

        return accountTypeMapper;
    }

    private AccountTypePredicateMapper<String> getAccountTypeFallbackMapper() {
        accountTypeFallbackMapper =
                Optional.ofNullable(accountTypeFallbackMapper)
                        .orElse(
                                AccountTypePredicateMapper.<String>builder()
                                        .setExecutor(new AvanzaV2AccountTypeMapperExecutor())
                                        .fallbackValue(
                                                AccountTypes.PENSION,
                                                codeMatches(AvanzaFallbackAccountTypes.PENSION))
                                        .fallbackValue(
                                                AccountTypes.SAVINGS,
                                                codeMatches(AvanzaFallbackAccountTypes.SPARKONTO))
                                        .fallbackValue(
                                                AccountTypes.LOAN,
                                                codeMatches(AvanzaFallbackAccountTypes.KREDIT))
                                        .build());

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
