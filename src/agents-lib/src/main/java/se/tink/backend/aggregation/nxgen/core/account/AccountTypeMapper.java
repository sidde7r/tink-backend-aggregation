package se.tink.backend.aggregation.nxgen.core.account;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.rpc.AccountTypes;

public class AccountTypeMapper {

    private static final Logger logger = LoggerFactory.getLogger(AccountTypeMapper.class);

    private final Map<String, AccountTypes> translator;
    private final Map<Pattern, AccountTypes> regexTranslator;

    public static class Builder {

        private final Map<AccountTypes, Object[]> reversed = new HashMap<>();
        private final Map<Pattern, AccountTypes> regexes = new HashMap<>();

        public AccountTypeMapper build() {
            return new AccountTypeMapper(this);
        }

        /**
         * Known keys, and the account type they should be mapped to.
         */
        public AccountTypeMapper.Builder put(AccountTypes value, Object... keys) {
            reversed.put(value, keys);
            return this;
        }

        /**
         * Known keys that should not be mapped to any specific account type.
         */
        public AccountTypeMapper.Builder ignoreKeys(Object... keys) {
            return this.put(AccountTypes.DUMMY, keys);
        }

        private Map<AccountTypes, Object[]> getReversed() {
            return reversed;
        }

        /**
         * A known key pattern, and the account type that a key matching it should be mapped to. The
         * mapping behavior is undefined if you specify two patterns with different associated
         * account types such that there exists a string that matches both.
         */
        public Builder putRegex(AccountTypes value, Pattern keyPattern) {
            regexes.put(keyPattern, value);
            return this;
        }
    }

    private AccountTypeMapper(AccountTypeMapper.Builder builder) {

        super();

        ImmutableMap.Builder<String, AccountTypes> tmpTranslator = ImmutableMap.builder();
        for (Map.Entry<AccountTypes, Object[]> entry : builder.getReversed().entrySet()) {
            for (Object key : entry.getValue()) {
                tmpTranslator.put(toKeyString(key), entry.getKey());
            }
        }
        translator = tmpTranslator.build();

        regexTranslator = builder.regexes;
    }

    public static AccountTypeMapper.Builder builder() {
        return new AccountTypeMapper.Builder();
    }

    private boolean verify(Object key, AccountTypes value) {
        Optional<AccountTypes> translated = translate(key);
        return translated.isPresent() && translated.get() == value;
    }

    private boolean verify(Object key, Collection<AccountTypes> values) {
        Optional<AccountTypes> translated = translate(key);
        return translated.isPresent() && values.contains(translated.get());
    }

    /**
     * @return The account type associated with the account type key, if such an explicit
     *     association was specified. Returns `Optional.empty` if the key was specified to be
     *     ignored. Otherwise, returns the account type associated with the regex pattern that this
     *     key matches, if any. If none of these conditions are met, returns `Optional.empty`.
     */
    public Optional<AccountTypes> translate(Object accountTypeKey) {

        AccountTypes type = translator.get(toKeyString(accountTypeKey));

        if (type == null) {
            return translateByRegex(String.valueOf(accountTypeKey));
        } else if (type == AccountTypes.DUMMY) {
            return Optional.empty();
        } else {
            return Optional.of(type);
        }
    }

    private Optional<AccountTypes> translateByRegex(String accountTypeString) {
        Set<Pattern> matchingPatterns = new HashSet<>();
        for (Map.Entry<Pattern, AccountTypes> patternToAccountType : regexTranslator.entrySet()) {
            if (patternToAccountType.getKey().matcher(accountTypeString).matches()) {
                matchingPatterns.add(patternToAccountType.getKey());
            }
        }
        Set<AccountTypes> associatedAccountTypes = regexTranslator.entrySet().stream()
                .filter(entry -> matchingPatterns.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());

        if (associatedAccountTypes.size() >= 2) {
            AccountTypes anyAccountType = associatedAccountTypes.iterator().next(); // Pop any of the elements
            logger.error(
                    "Account type string: \"{}\" matched multiple regexes with different associated account types: {} -- using {}",
                    accountTypeString,
                    matchingPatterns,
                    anyAccountType);
            return Optional.of(anyAccountType);
        } else if (associatedAccountTypes.size() == 1) {
            Pattern matchingPattern = matchingPatterns.iterator().next();
            AccountTypes associatedAccountType = associatedAccountTypes.iterator().next();
            logger.warn(
                    "Unspecified account type for key: \"{}\" -- using {} because it matches pattern {}",
                    accountTypeString,
                    associatedAccountType,
                    matchingPattern);
            return Optional.of(associatedAccountType);
        }

        logger.warn("Unknown account type for key: {}", accountTypeString);
        return Optional.empty();
    }

    private static String toKeyString(Object accountTypeKey) {
        return String.valueOf(accountTypeKey).toLowerCase();
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
}
