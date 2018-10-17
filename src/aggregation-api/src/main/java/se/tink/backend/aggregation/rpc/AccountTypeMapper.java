package se.tink.backend.aggregation.rpc;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class AccountTypeMapper {

    private static final Logger logger = LoggerFactory.getLogger(AccountTypeMapper.class);

    private final Map<String, AccountTypes> translator;

    public static class Builder {

        private final Map<AccountTypes, String[]> reversed = new HashMap<>();

        public AccountTypeMapper build() {
            return new AccountTypeMapper(this);
        }

        /**
         * Known keys, and the account type they should be mapped to.
         */
        public AccountTypeMapper.Builder add(AccountTypes value, String... keys) {
            reversed.put(value, keys);
            return this;
        }

        /**
         * Known keys that should not be mapped to any specific account type.
         */
        public AccountTypeMapper.Builder add(String... keys) {
            return this.add(AccountTypes.DUMMY, keys);
        }

        Map<AccountTypes, String[]> getReversed() {
            return reversed;
        }
    }

    AccountTypeMapper(AccountTypeMapper.Builder builder) {

        super();

        ImmutableMap.Builder<String, AccountTypes> tmpTranslator = ImmutableMap.builder();
        for (Map.Entry<AccountTypes, String[]> entry : builder.getReversed().entrySet()) {
            for (String key : entry.getValue()) {
                tmpTranslator.put(key.toLowerCase(), entry.getKey());
            }
        }
        translator = tmpTranslator.build();
    }

    public static AccountTypeMapper.Builder builder() {
        return new AccountTypeMapper.Builder();
    }

    public boolean verify(String key, AccountTypes value) {
        Optional<AccountTypes> translated = translate(key);
        return translated.isPresent() && translated.get() == value;
    }

    public boolean verify(String key, Collection<AccountTypes> values) {
        Optional<AccountTypes> translated = translate(key);
        return translated.isPresent() && values.contains(translated.get());
    }

    public Optional<AccountTypes> translate(String accountTypeKey) {

        if (Strings.isNullOrEmpty(accountTypeKey)) {
            return Optional.empty();
        }

        AccountTypes type = translator.get(accountTypeKey.toLowerCase());

        if (type == null) {
            logger.warn("Unknown account type for type key: {}", accountTypeKey);
            return Optional.empty();
        } else if (type == AccountTypes.DUMMY) {
            return Optional.empty();
        } else {
            return Optional.of(type);
        }
    }

    public boolean isInvestmentAccount(String accountTypeKey) {
        return verify(accountTypeKey, InvestmentAccount.ALLOWED_ACCOUNT_TYPES);
    }

    public boolean isTransactionalAccount(String accountTypeKey) {
        return verify(accountTypeKey, TransactionalAccount.ALLOWED_ACCOUNT_TYPES);
    }

    public boolean isSavingsAccount(String accountTypeKey) {
        return verify(accountTypeKey, AccountTypes.SAVINGS);
    }

    public boolean isCheckingAccount(String accountTypeKey) {
        return verify(accountTypeKey, AccountTypes.CHECKING);
    }

    public boolean isLoanAccount(String accountTypeKey) {
        return verify(accountTypeKey, AccountTypes.LOAN);
    }

    public boolean isCreditCardAccount(String accountTypeKey) {
        return verify(accountTypeKey, AccountTypes.CREDIT_CARD);
    }

}
