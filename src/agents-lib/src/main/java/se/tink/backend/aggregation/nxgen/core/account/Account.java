package se.tink.backend.aggregation.nxgen.core.account;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.aggregation.rpc.User;
import se.tink.backend.core.AccountFlag;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.enums.FeatureFlags;
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
    private TemporaryStorage temporaryStorage;
    private List<AccountFlag> accountFlags;

    protected Account(Builder<? extends Account, ? extends Account.Builder> builder) {
        this.name = builder.getName();
        this.accountNumber = builder.getAccountNumber();
        this.balance = builder.getBalance();
        this.identifiers = ImmutableList.copyOf(builder.getIdentifiers());
        this.uniqueIdentifier = sanitizeUniqueIdentifier(builder.getUniqueIdentifier());
        this.bankIdentifier = builder.getBankIdentifier();
        this.holderName = builder.getHolderName();
        this.temporaryStorage = builder.getTransientStorage();
        this.accountFlags = ImmutableList.copyOf(builder.getAccountFlags());
        // Safe-guard against uniqueIdentifiers containing only formatting characters (e.g. '*' or '-').
        Preconditions.checkState(!Strings.isNullOrEmpty(uniqueIdentifier),
                "Unique identifier was empty after sanitation.");
    }

    public static Builder<? extends Account, ?> builder(AccountTypes type, String uniqueIdentifier) {
        switch (type) {
        case SAVINGS:
        case OTHER:
        case CHECKING:
            return TransactionalAccount.builder(type, uniqueIdentifier);
        case CREDIT_CARD:
            return CreditCardAccount.builder(uniqueIdentifier);
        case PENSION:
        case INVESTMENT:
            return InvestmentAccount.builder(uniqueIdentifier);
        case MORTGAGE:
        case LOAN:
            return LoanAccount.builder(uniqueIdentifier);
        default:
            throw new IllegalStateException(
                    String.format("Unknown Account type (%s)", type));
        }
    }

    private String sanitizeUniqueIdentifier(String uniqueIdentifier) {
        return uniqueIdentifier.replaceAll("[^\\dA-Za-z]", "");
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

    public List<AccountFlag> getAccountFlags() {
        return Lists.newArrayList(this.accountFlags);
    }

    private String getUniqueIdentifier() {
        return this.uniqueIdentifier;
    }

    public boolean isUniqueIdentifierEqual(String otherUniqueIdentifier) {
        if (Strings.isNullOrEmpty(otherUniqueIdentifier)) {
            return false;
        }

        return this.uniqueIdentifier.equals(sanitizeUniqueIdentifier(otherUniqueIdentifier));
    }

    /**
     * @return Unique identifier on the bank side, not to be confused with rpc Account.getBankId
     */
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

    public se.tink.backend.aggregation.rpc.Account toSystemAccount(User user) {
        se.tink.backend.aggregation.rpc.Account account = new se.tink.backend.aggregation.rpc.Account();

        account.setType(getType());
        account.setName(this.name);
        account.setAccountNumber(this.accountNumber);
        account.setBalance(this.balance.getValue());
        account.setIdentifiers(this.identifiers);
        account.setBankId(this.uniqueIdentifier);
        account.setHolderName(HolderName.toString(this.holderName));
        account.setFlags(this.accountFlags);
        account.setPayload(createPayload(user));

        return account;
    }

    public HolderName getHolderName() {
        return this.holderName;
    }

    public String getFromTemporaryStorage(String key) {
        return temporaryStorage.get(key);
    }

    public <T> Optional<T> getFromTemporaryStorage(String key, Class<T> valueType) {
        return temporaryStorage.get(key, valueType);
    }

    public <T> Optional<T> getFromTemporaryStorage(String key, TypeReference<T> valueType) {
        return temporaryStorage.get(key, valueType);
    }

    private String createPayload(User user) {
        if (!FeatureFlags.FeatureFlagGroup.MULTI_CURRENCY_FOR_POCS.isFlagInGroup(user.getFlags())) {
            return null;
        }

        HashMap<String, String> map = Maps.newHashMap();
        map.put("currency", balance.getCurrency());
        return SerializationUtils.serializeToString(map);
    }

    public abstract static class Builder<A extends Account, T extends Builder<A, T>> {
        protected final List<AccountIdentifier> identifiers = Lists.newArrayList();
        protected final List<AccountFlag> accountFlags = Lists.newArrayList();
        protected final TemporaryStorage temporaryStorage = new TemporaryStorage();
        protected String name;
        protected String accountNumber;
        protected Amount balance;
        protected String uniqueIdentifier;
        protected HolderName holderName;
        private T thisObj;

        protected Builder(String uniqueIdentifier) {
            this.thisObj = self();

            Preconditions.checkArgument(!Strings.isNullOrEmpty(uniqueIdentifier),
                    "Unique identifier is null or empty.");
            this.thisObj.uniqueIdentifier = uniqueIdentifier;
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

        public List<AccountFlag> getAccountFlags() {
            return thisObj.accountFlags != null ? thisObj.accountFlags : Collections.emptyList();
        }

        public T addAccountFlag(AccountFlag accountFlag) {
            thisObj.accountFlags.add(accountFlag);
            return self();
        }


        public T addAccountFlags(Collection<AccountFlag> accountFlags){
            thisObj.accountFlags.addAll(accountFlags);
            return self();
        }

        public String getUniqueIdentifier() {
            return Preconditions.checkNotNull(thisObj.uniqueIdentifier, "Unique identifier must be set.");
        }

        public HolderName getHolderName() {
            return thisObj.holderName;
        }

        public T setHolderName(HolderName holderName) {
            thisObj.holderName = holderName;
            return self();
        }

        public T setBankIdentifier(String bankIdentifier) {
            temporaryStorage.put(BANK_IDENTIFIER_KEY, bankIdentifier);
            return self();
        }

        public <K> T putInTemporaryStorage(String key, K value) {
            temporaryStorage.put(key, value);
            return self();
        }

        private String getBankIdentifier() {
            return temporaryStorage.get(BANK_IDENTIFIER_KEY);
        }

        private TemporaryStorage getTransientStorage() {
            return temporaryStorage;
        }

        public abstract A build();
    }
}
