package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.AvanzaAccountTypes;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.AvanzaFallbackAccountTypes;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.LogTags;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapperExecutor;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypePredicateMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.pair.Pair;

public final class AvanzaAccountTypeMappers {

    private static final Logger LOGGER = LoggerFactory.getLogger(AvanzaAccountTypeMappers.class);

    private TypeMapper<AccountTypes> accountTypeMapper;
    private AccountTypePredicateMapper<String> accountTypeFallbackMapper;
    private TypeMapper<LoanDetails.Type> loanTypeMapper;

    private static Predicate<String> codeMatches(final String regex) {
        return t -> Pattern.compile(regex).matcher(t).matches();
    }

    public Optional<AccountTypes> inferAccountType(final String accountTypeKey) {
        final Optional<AccountTypes> accountType = getAccountTypeMapper().translate(accountTypeKey);

        if (accountType.isPresent()) {
            return accountType;
        }

        return inferFallbackAccountType(accountTypeKey);
    }

    private Optional<AccountTypes> inferFallbackAccountType(final String accountTypeKey) {
        final Optional<AccountTypes> accountType =
                getAccountTypeFallbackMapper().translate(accountTypeKey);

        // TODO refactor with isPresentOrElse when we are past Java 8
        if (!accountType.isPresent()) {
            LOGGER.warn(
                    "{} Could not infer account type from type \"{}\"; ignoring the account",
                    LogTags.UNKNOWN_ACCOUNT_TYPE,
                    accountTypeKey);
        }

        return accountType;
    }

    private TypeMapper<AccountTypes> getAccountTypeMapper() {
        accountTypeMapper =
                Optional.ofNullable(accountTypeMapper)
                        .orElse(
                                TypeMapper.<AccountTypes>builder()
                                        .put(
                                                AccountTypes.INVESTMENT,
                                                AvanzaAccountTypes.AKTIE_FONDKONTO,
                                                AvanzaAccountTypes.INVESTERINGSSPARKONTO,
                                                AvanzaAccountTypes.KAPITALFORSAKRING,
                                                AvanzaAccountTypes.KAPITALFORSAKRING_BARN)
                                        .put(
                                                AccountTypes.SAVINGS,
                                                AvanzaAccountTypes.SPARKONTO,
                                                AvanzaAccountTypes.SPARKONTOPLUS)
                                        .put(
                                                AccountTypes.PENSION,
                                                AvanzaAccountTypes.TJANSTEPENSION,
                                                AvanzaAccountTypes.PENSIONSFORSAKRING,
                                                AvanzaAccountTypes.IPS,
                                                AvanzaAccountTypes.AVTALS_PENSION)
                                        .put(
                                                AccountTypes.LOAN,
                                                AvanzaAccountTypes.SUPER_BOLANEKONTO,
                                                AvanzaAccountTypes.KREDITKONTO_ISK,
                                                AvanzaAccountTypes.KREDITKONTO_KF)
                                        .build());

        return accountTypeMapper;
    }

    private AccountTypePredicateMapper<String> getAccountTypeFallbackMapper() {
        accountTypeFallbackMapper =
                Optional.ofNullable(accountTypeFallbackMapper)
                        .orElse(
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
                                        .build());

        return accountTypeFallbackMapper;
    }

    private TypeMapper<LoanDetails.Type> getLoanTypeMapper() {
        loanTypeMapper =
                Optional.ofNullable(loanTypeMapper)
                        .orElse(
                                TypeMapper.<LoanDetails.Type>builder()
                                        .put(
                                                LoanDetails.Type.BLANCO,
                                                AvanzaAccountTypes.KREDITKONTO_ISK,
                                                AvanzaAccountTypes.KREDITKONTO_KF)
                                        .put(
                                                LoanDetails.Type.MORTGAGE,
                                                AvanzaAccountTypes.SUPER_BOLANEKONTO)
                                        .build());
        return loanTypeMapper;
    }

    public Optional<LoanDetails.Type> getLoanType(String loanType) {
        return getLoanTypeMapper().translate(loanType);
    }

    private boolean verify(String key, AccountTypes value) {
        return inferAccountType(key).map(x -> x == value).orElse(false);
    }

    private boolean verify(String key, Collection<AccountTypes> values) {
        return inferAccountType(key).map(values::contains).orElse(false);
    }

    public boolean isInvestmentAccount(String accountTypeKey) {
        return verify(accountTypeKey, InvestmentAccount.ALLOWED_ACCOUNT_TYPES);
    }

    public boolean isTransactionalAccount(String accountTypeKey) {
        return verify(accountTypeKey, TransactionalAccount.ALLOWED_ACCOUNT_TYPES);
    }

    public boolean isLoanAccount(String accountTypeKey) {
        return verify(accountTypeKey, AccountTypes.LOAN);
    }

    public boolean isPensionAccount(String accountTypeKey) {
        return verify(accountTypeKey, AccountTypes.PENSION);
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
