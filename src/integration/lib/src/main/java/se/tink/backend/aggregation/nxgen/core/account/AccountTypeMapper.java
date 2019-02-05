package se.tink.backend.aggregation.nxgen.core.account;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountTypes;

/**
 * @deprecated Use {@link se.tink.backend.aggregation.nxgen.core.account.TypeMapper} instead.
 */
@Deprecated
public class AccountTypeMapper {

    private static final Logger logger = LoggerFactory.getLogger(AccountTypeMapper.class);

    private final Map<String, AccountTypes> translator;

    public static class Builder {

        private final Map<AccountTypes, Object[]> reversed = new HashMap<>();

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

    public Optional<AccountTypes> translate(Object accountTypeKey) {

        AccountTypes type = translator.get(toKeyString(accountTypeKey));

        if (type == null) {
            logger.warn("Unknown account type for key: {}", accountTypeKey);
            return Optional.empty();
        } else if (type == AccountTypes.DUMMY) {
            return Optional.empty();
        } else {
            return Optional.of(type);
        }
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
