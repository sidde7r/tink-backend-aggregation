package se.tink.backend.aggregation.nxgen.core.account;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
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
    private String name;
    private String accountNumber;
    private Amount balance;
    private List<AccountIdentifier> identifiers;
    private String uniqueIdentifier;
    private String bankIdentifier;
    private HolderName holderName;
    private Map<String, String> temporaryStorage;

    protected Account(Builder<? extends Account, ? extends Account.Builder> builder) {
        this.name = builder.getName();
        this.accountNumber = builder.getAccountNumber();
        this.balance = builder.getBalance();
        this.identifiers = ImmutableList.copyOf(builder.getIdentifiers());
        this.uniqueIdentifier = builder.getUniqueIdentifier();
        this.bankIdentifier = builder.getBankIdentifier();
        this.holderName = builder.getHolderName();
        this.temporaryStorage = ImmutableMap.copyOf(builder.getTemporaryStorage());
    }

    public static Builder<? extends Account, ?> builder(AccountTypes type) {
        switch (type) {
        case SAVINGS:
        case OTHER:
        case CHECKING:
            return TransactionalAccount.builder(type);
        case CREDIT_CARD:
            return CreditCardAccount.builder();
        case INVESTMENT:
            return InvestmentAccount.builder();
        case LOAN:
            return LoanAccount.builder();
        default:
            throw new IllegalStateException(
                    String.format("Unknown Account type (%s)", type));
        }
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

    public abstract static class Builder<A extends Account, T extends Builder<A, T>> {
        protected final List<AccountIdentifier> identifiers = Lists.newArrayList();
        protected final Map<String, String> temporaryStorage = Maps.newHashMap();
        protected String name;
        protected String accountNumber;
        protected Amount balance;
        protected String uniqueIdentifier;
        protected HolderName holderName;
        private T thisObj;

        protected Builder() {
            this.thisObj = self();
        }

        protected abstract T self();

        public String getName() {
            return Strings.isNullOrEmpty(thisObj.name) ? getAccountNumber() : thisObj.name;
        }

        public T setName(String name) {
            thisObj.name = name;
            return self();
        }

        public String getAccountNumber() {
            return Preconditions.checkNotNull(Strings.emptyToNull(thisObj.accountNumber));
        }

        public T setAccountNumber(String accountNumber) {
            thisObj.accountNumber = accountNumber;
            return self();
        }

        public Amount getBalance() {
            return Amount.createFromAmount(thisObj.balance).orElseThrow(NullPointerException::new);
        }

        public T setBalance(Amount balance) {
            thisObj.balance = balance;
            return self();
        }

        public List<AccountIdentifier> getIdentifiers() {
            return thisObj.identifiers != null ? thisObj.identifiers : Collections.emptyList();
        }

        public T addIdentifier(AccountIdentifier identifier) {
            thisObj.identifiers.add(identifier);
            return self();
        }

        public T addIdentifiers(Collection<AccountIdentifier> identifiers){
            thisObj.identifiers.addAll(identifiers);
            return self();
        }

        public String getUniqueIdentifier() {
            return !Strings.isNullOrEmpty(thisObj.uniqueIdentifier)
                    ? thisObj.uniqueIdentifier
                    : getAccountNumber();
        }

        public T setUniqueIdentifier(String uniqueIdentifier) {
            thisObj.uniqueIdentifier = uniqueIdentifier;
            return self();
        }

        public String getBankIdentifier() {
            String bankIdentifier = getTemporaryStorage().get(BANK_IDENTIFIER_KEY);
            return java.util.Objects.nonNull(bankIdentifier)
                    ? SerializationUtils.deserializeFromString(bankIdentifier, String.class)
                    : getUniqueIdentifier();
        }

        public T setBankIdentifier(String bankIdentifier) {
            addToTemporaryStorage(BANK_IDENTIFIER_KEY, bankIdentifier);
            return self();
        }

        public HolderName getHolderName() {
            return thisObj.holderName;
        }

        public T setHolderName(HolderName holderName) {
            thisObj.holderName = holderName;
            return self();
        }

        public <K> T addToTemporaryStorage(String key, K value) {
            temporaryStorage.put(key, SerializationUtils.serializeToString(value));
            return self();
        }

        public Map<String, String> getTemporaryStorage() {
            return java.util.Objects.nonNull(temporaryStorage)
                    ? temporaryStorage
                    : Collections.emptyMap();
        }

        public T setTemporaryStorage(Map<String, String> temporaryStorage) {
            thisObj.temporaryStorage.putAll(temporaryStorage);
            return self();
        }

        public abstract A build();
    }
}
