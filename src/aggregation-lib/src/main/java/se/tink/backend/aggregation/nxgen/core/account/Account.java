package se.tink.backend.aggregation.nxgen.core.account;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.serialization.utils.SerializationUtils;

public abstract class Account {
    private static final String BANK_IDENTIFIER_KEY = "bankIdentifier";
    private final String name;
    private final String accountNumber;
    private final Amount balance;
    private final List<AccountIdentifier> identifiers;
    private final String uniqueIdentifier;
    private final String bankIdentifier;
    private final HolderName holderName;
    private final Map<String, String> temporaryStorage;

    Account(String name, String accountNumber, Amount balance, List<AccountIdentifier> identifiers,
            String uniqueIdentifier, String bankIdentifier, HolderName holderName,
            Map<String, String> temporaryStorage) {
        this.name = name;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.identifiers = identifiers;
        this.uniqueIdentifier = uniqueIdentifier;
        this.bankIdentifier = bankIdentifier;
        this.holderName = holderName;
        this.temporaryStorage = temporaryStorage;
    }

    public Account(String name, String accountNumber, Amount balance, List<AccountIdentifier> identifiers,
            String uniqueIdentifier, String bankIdentifier) {
        this(name, accountNumber, balance, identifiers, uniqueIdentifier, bankIdentifier, null, Collections.emptyMap());
    }

    public AccountTypes getType() {
        return AccountTypes.CHECKING;
    }

    public String getName() {
        return this.name;
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    public Amount getBalance() {
        return new Amount(this.balance.getCurrency(), this.balance.getValue());
    }

    public List<AccountIdentifier> getIdentifiers() {
        return Lists.newArrayList(this.identifiers);
    }

    public String getUniqueIdentifier() {
        return this.uniqueIdentifier;
    }

    public String getBankIdentifier() {
        return this.bankIdentifier;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Account && Objects.equal(hashCode(), obj.hashCode());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUniqueIdentifier());
    }

    public se.tink.backend.aggregation.rpc.Account toSystemAccount() {
        se.tink.backend.aggregation.rpc.Account account = new se.tink.backend.aggregation.rpc.Account();

        account.setType(getType());
        account.setName(this.name);
        account.setAccountNumber(this.accountNumber);
        account.setBalance(this.balance.getValue());
        account.setIdentifiers(this.identifiers);
        account.setBankId(this.uniqueIdentifier.replaceAll("[^\\dA-Za-z]", ""));
        account.setHolderName(HolderName.toString(this.holderName));

        return account;
    }

    public HolderName getHolderName() {
        return this.holderName;
    }

    public Map<String, String> getTemporaryStorage() {
        return temporaryStorage;
    }

    public <T> T getTemporaryStorage(String key, Class<T> clazz) {
        if (temporaryStorage == null) {
            return null;
        }

        return SerializationUtils.deserializeFromString(temporaryStorage.get(key), clazz);
    }

    public <T> void addToTemporaryStorage(String key, T value) {
        temporaryStorage.put(key, SerializationUtils.serializeToString(value));
    }

    public static abstract class Builder {
        private String name;
        private final String accountNumber;
        private final Amount balance;
        private final List<AccountIdentifier> identifiers = Lists.newArrayList();
        private String uniqueIdentifier;
        private HolderName holderName;
        private Map<String, String> temporaryStorage;

        protected Builder(String accountNumber, Amount balance) {
            this.accountNumber = accountNumber;
            this.balance = balance;
        }

        public String getName() {
            return Strings.isNullOrEmpty(this.name) ? getAccountNumber() : this.name;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public String getAccountNumber() {
            return Preconditions.checkNotNull(Strings.emptyToNull(this.accountNumber));
        }

        public Amount getBalance() {
            return Amount.createFromAmount(this.balance).orElseThrow(NullPointerException::new);
        }

        public List<AccountIdentifier> getIdentifiers() {
            return this.identifiers != null ? this.identifiers : Collections.emptyList();
        }

        public Builder addIdentifier(AccountIdentifier identifier) {
            this.identifiers.add(identifier);
            return this;
        }

        public String getUniqueIdentifier() {
            return !Strings.isNullOrEmpty(this.uniqueIdentifier) ? this.uniqueIdentifier : getAccountNumber();
        }

        public Builder setUniqueIdentifier(String uniqueIdentifier) {
            this.uniqueIdentifier = uniqueIdentifier;
            return this;
        }

        public String getBankIdentifier() {
            String bankIdentifier = getTemporaryStorage().get(BANK_IDENTIFIER_KEY);
            return java.util.Objects.nonNull(bankIdentifier) ?
                    SerializationUtils.deserializeFromString(bankIdentifier, String.class) : getUniqueIdentifier();
        }

        public Builder setBankIdentifier(String bankIdentifier) {
            addToTemporaryStorage(BANK_IDENTIFIER_KEY, bankIdentifier);
            return this;
        }

        public HolderName getHolderName() {
            return this.holderName;
        }

        public Builder setHolderName(HolderName holderName) {
            this.holderName = holderName;
            return this;
        }

        public Builder setTemporaryStorage(Map<String, String> temporaryStorage) {
            this.temporaryStorage = temporaryStorage;
            return this;
        }

        public <T> Builder addToTemporaryStorage(String key, T value) {
            if (temporaryStorage == null) {
                temporaryStorage = Maps.newHashMap();
            }

            temporaryStorage.put(key, SerializationUtils.serializeToString(value));
            return this;
        }

        public Map<String, String> getTemporaryStorage() {
            return java.util.Objects.nonNull(temporaryStorage) ? temporaryStorage : Collections.emptyMap();
        }

        public abstract Account build();
    }
}
