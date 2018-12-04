package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.AvanzaAccountTypes;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.AvanzaFallbackAccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapperExecutor;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypePredicateMapper;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.common.utils.Pair;

public final class AvanzaAccountTypeMappers {
    private static final Logger LOGGER = LoggerFactory.getLogger(AvanzaAccountTypeMappers.class);

    private AccountTypeMapper accountTypeMapper;
    private AccountTypePredicateMapper<String> accountTypeFallbackMapper;

    private static Predicate<String> codeMatches(final String regex) {
        return t -> Pattern.compile(regex).matcher(t).matches();
    }

    private static String toKeyString(Object accountTypeKey) {
        return String.valueOf(accountTypeKey).toLowerCase();
    }

    public Optional<AccountTypes> inferAccountType(final String accountTypeKey) {
        final Optional<AccountTypes> accountType =
                Optional.of(getAccountTypeMapper().translate(accountTypeKey))
                        .orElse(getAccountTypeFallbackMapper().translate(accountTypeKey));

        // TODO refactor with isPresentOrElse when we are past Java 8
        if (!accountType.isPresent()) {
            LOGGER.warn(
                    "{} Could not infer account type from type \"{}\"; ignoring the account",
                    AvanzaConstants.LogTags.UNKNOWN_ACCOUNT_TYPE,
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
                            .build();
        }
        return accountTypeMapper;
    }

    private AccountTypePredicateMapper<String> getAccountTypeFallbackMapper() {
        if (accountTypeFallbackMapper == null) {
            accountTypeFallbackMapper =
                    AccountTypePredicateMapper.<String>builder()
                            .setExecutor(new AvanzaAccountTypeMapperExecutor())
                            .fallbackValue(
                                    AccountTypes.PENSION,
                                    codeMatches(AvanzaFallbackAccountTypes.PENSION))
                            .fallbackValue(
                                    AccountTypes.SAVINGS,
                                    codeMatches(AvanzaFallbackAccountTypes.SPARKONTO))
                            .fallbackValue(
                                    AccountTypes.LOAN,
                                    codeMatches(AvanzaFallbackAccountTypes.KREDIT))
                            .build();
        }
        return accountTypeFallbackMapper;
    }

    private boolean verify(Object key, AccountTypes value) {
        Optional<AccountTypes> inferred = inferAccountType(toKeyString(key));
        return inferred.isPresent() && inferred.get() == value;
    }

    private boolean verify(Object key, Collection<AccountTypes> values) {
        Optional<AccountTypes> inferred = inferAccountType(toKeyString(key));
        return inferred.isPresent() && values.contains(inferred.get());
    }

    public boolean isInvestmentAccount(Object accountTypeKey) {
        return verify(accountTypeKey, InvestmentAccount.ALLOWED_ACCOUNT_TYPES);
    }

    public boolean isTransactionalAccount(Object accountTypeKey) {
        return verify(accountTypeKey, TransactionalAccount.ALLOWED_ACCOUNT_TYPES);
    }

    public boolean isTransactionalAccount(AccountTypes type) {
        return TransactionalAccount.ALLOWED_ACCOUNT_TYPES.contains(type);
    }

    public boolean isSavingsAccount(Object accountTypeKey) {
        return verify(accountTypeKey, AccountTypes.SAVINGS);
    }

    public boolean isCheckingAccount(Object accountTypeKey) {
        return verify(accountTypeKey, AccountTypes.CHECKING);
    }

    public boolean isLoanAccount(Object accountTypeKey) {
        return verify(accountTypeKey, AccountTypes.LOAN);
    }

    public boolean isCreditCardAccount(Object accountTypeKey) {
        return verify(accountTypeKey, AccountTypes.CREDIT_CARD);
    }

    public boolean isCreditCardAccount(AccountTypes type) {
        return type == AccountTypes.CREDIT_CARD;
    }

    private class AvanzaAccountTypeMapperExecutor implements AccountTypeMapperExecutor<String> {
        private final Logger LOGGER =
                LoggerFactory.getLogger(AvanzaAccountTypeMapperExecutor.class);

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
