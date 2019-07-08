package se.tink.backend.aggregation.nxgen.core.account;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.BuildStep;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.FeatureFlags;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.strings.StringUtils;
import se.tink.libraries.user.rpc.User;

public abstract class Account {
    static final String BANK_IDENTIFIER_KEY = "bankIdentifier";
    private static final Logger logger = LoggerFactory.getLogger(Account.class);
    protected BalanceModule balanceModule;
    protected IdModule idModule;
    protected String name;
    protected String productName;
    protected String accountNumber;
    protected Set<AccountIdentifier> identifiers;
    protected String uniqueIdentifier;
    protected String apiIdentifier;
    protected HolderName holderName;
    protected TemporaryStorage temporaryStorage;
    protected Set<AccountFlag> accountFlags;
    private ExactCurrencyAmount exactBalance;
    private ExactCurrencyAmount exactAvailableCredit;

    // Exists for interoperability only, do not ever use
    protected Account(AccountBuilder<? extends Account, ?> builder) {
        // These exist for interoperability and will eventually be removed
        this.name = builder.getIdModule().getAccountName();
        this.accountNumber = builder.getIdModule().getAccountNumber();
        this.exactAvailableCredit =
                builder.getBalanceModule().getExactAvaliableCredit().orElse(null);
        this.exactBalance = builder.getBalanceModule().getExactBalance();
        this.identifiers = builder.getIdModule().getIdentifiers();
        this.uniqueIdentifier = builder.getIdModule().getUniqueId();
        this.balanceModule = builder.getBalanceModule();
        this.idModule = builder.getIdModule();
        this.apiIdentifier = builder.getApiIdentifier();
        this.holderName = builder.getHolderNames().stream().findFirst().orElse(null);
        this.temporaryStorage = builder.getTransientStorage();
        this.accountFlags = ImmutableSet.copyOf(builder.getAccountFlags());
    }

    // This will be removed as part of the improved step builder + agent builder refactoring project
    @Deprecated
    protected Account(Builder<? extends Account, ? extends Account.Builder> builder) {
        this.name = builder.getName();
        this.accountNumber = builder.getAccountNumber();
        this.identifiers = ImmutableSet.copyOf(builder.getIdentifiers());
        this.uniqueIdentifier = sanitizeUniqueIdentifier(builder.getUniqueIdentifier());
        this.apiIdentifier = builder.getBankIdentifier();
        this.holderName = builder.getHolderName();
        this.temporaryStorage = builder.getTransientStorage();
        this.accountFlags = ImmutableSet.copyOf(builder.getAccountFlags());
        this.exactBalance = builder.getExactBalance();
        this.exactAvailableCredit =
                Optional.ofNullable(builder.getExactAvailableCredit()).orElse(null);
        // Safe-guard against uniqueIdentifiers containing only formatting characters (e.g. '*' or
        // '-').
        Preconditions.checkState(
                !Strings.isNullOrEmpty(uniqueIdentifier),
                "Unique identifier was empty after sanitation.");
    }

    protected Account(StepBuilder<? extends Account, ?> builder) {
        this.accountNumber = builder.getAccountNumber();
        this.apiIdentifier = builder.getApiIdentifier();
        this.exactBalance =
                ExactCurrencyAmount.of(
                        builder.getBalance().doubleValue(), builder.getBalance().getCurrency());
        this.identifiers = ImmutableSet.copyOf(builder.getIdentifiers());
        this.uniqueIdentifier = builder.getUniqueIdentifier();
        this.temporaryStorage = builder.getTemporaryStorage();
        this.accountFlags = ImmutableSet.copyOf(builder.getAccountFlags());
        this.productName = builder.getProductName();

        if (Strings.isNullOrEmpty(builder.getAlias())) {
            // Fallback in case the received alias happened to be null at run-time.
            // Indicates a programming fault because the agent should be implemented in a way such
            // that it always sets the alias to the displayed name of the account.
            logger.error("Supplied alias was null -- falling back to account number");
            this.name = builder.getAccountNumber();
        } else {
            this.name = builder.getAlias();
        }

        // Only use one holder name for now
        this.holderName =
                new HolderName(builder.getHolderNames().stream().findFirst().orElse(null));
    }

    @Deprecated
    public static Builder<? extends Account, ?> builder(
            AccountTypes type, String uniqueIdentifier) {
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
                throw new IllegalStateException(String.format("Unknown Account type (%s)", type));
        }
    }

    private String sanitizeUniqueIdentifier(String uniqueIdentifier) {
        return uniqueIdentifier.replaceAll("[^\\dA-Za-z]", "");
    }

    public abstract AccountTypes getType();

    public String getName() {
        return this.name;
    }

    public String getProductName() {
        return productName;
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    @Deprecated
    public Amount getBalance() {
        return Optional.ofNullable(exactBalance)
                .map(e -> new Amount(e.getCurrencyCode(), e.getDoubleValue()))
                .orElseGet(
                        () ->
                                new Amount(
                                        balanceModule.getExactBalance().getCurrencyCode(),
                                        balanceModule.getExactBalance().getDoubleValue()));
    }

    public ExactCurrencyAmount getExactBalance() {
        return Optional.ofNullable(exactBalance)
                .map(ExactCurrencyAmount::of)
                .orElseGet(
                        () ->
                                ExactCurrencyAmount.of(
                                        balanceModule.getExactBalance().getDoubleValue(),
                                        balanceModule.getExactBalance().getCurrencyCode()));
    }

    public BalanceModule getBalanceModule() {
        return balanceModule;
    }

    public IdModule getIdModule() {
        return idModule;
    }

    @Deprecated
    public Amount getAccountBalance() {
        return new Amount(this.exactBalance.getCurrencyCode(), this.exactBalance.getDoubleValue());
    }

    @Deprecated
    public Amount getAvailableCredit() {
        return new Amount(
                this.getExactAvailableCredit().getCurrencyCode(),
                this.getExactAvailableCredit().getDoubleValue());
    }

    public ExactCurrencyAmount getExactAccountBalance() {
        return ExactCurrencyAmount.of(exactBalance);
    }

    public ExactCurrencyAmount getExactAvailableCredit() {
        return ExactCurrencyAmount.of(exactAvailableCredit);
    }

    public List<AccountIdentifier> getIdentifiers() {
        return Lists.newArrayList(this.identifiers);
    }

    public List<AccountFlag> getAccountFlags() {
        return Lists.newArrayList(this.accountFlags);
    }

    String getUniqueIdentifier() {
        return this.uniqueIdentifier;
    }

    public boolean isUniqueIdentifierEqual(String otherUniqueIdentifier) {
        if (Strings.isNullOrEmpty(otherUniqueIdentifier)) {
            return false;
        }

        return this.uniqueIdentifier.equals(sanitizeUniqueIdentifier(otherUniqueIdentifier));
    }

    /**
     * @deprecated Use getApiIdentifier() instead.
     * @return Unique identifier on the bank side, not to be confused with rpc Account.getBankId
     */
    @Deprecated
    public String getBankIdentifier() {
        return this.apiIdentifier;
    }

    /** @return Unique identifier on the bank side, not to be confused with rpc Account.getBankId */
    public String getApiIdentifier() {
        return this.apiIdentifier;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Account && Objects.equal(hashCode(), obj.hashCode());
    }

    @Override
    public int hashCode() {
        return getUniqueIdentifier().hashCode();
    }

    public se.tink.backend.agents.rpc.Account toSystemAccount(User user) {
        se.tink.backend.agents.rpc.Account account = new se.tink.backend.agents.rpc.Account();

        account.setType(getType());
        account.setName(this.name);
        account.setAccountNumber(this.accountNumber);
        account.setBalance(this.exactBalance.getDoubleValue());
        account.setCurrencyCode(this.exactBalance.getCurrencyCode());
        account.setExactBalance(this.exactBalance);
        account.setIdentifiers(this.identifiers);
        account.setBankId(this.uniqueIdentifier);
        account.setHolderName(HolderName.toString(this.holderName));
        account.setFlags(this.accountFlags);
        account.setPayload(createPayload(user));
        account.setAvailableCredit(
                Optional.ofNullable(this.exactAvailableCredit)
                        .map(ExactCurrencyAmount::getDoubleValue)
                        .orElse(0.0));
        account.setExactAvailableCredit(
                Optional.ofNullable(this.exactAvailableCredit).orElse(null));

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
        map.put("currency", exactBalance.getCurrencyCode());
        return SerializationUtils.serializeToString(map);
    }

    // This will be removed as part of the improved step builder + agent builder refactoring project
    @Deprecated
    public abstract static class StepBuilder<A extends Account, B extends BuildStep<A, B>>
            implements BuildStep<A, B> {

        private final Collection<String> holderNames = Lists.newArrayList();
        private final Set<AccountIdentifier> identifiers = new HashSet<>();
        private final TemporaryStorage temporaryStorage = new TemporaryStorage();
        private final Set<AccountFlag> accountFlags = new HashSet<>();
        private String uniqueIdentifier;
        private String accountNumber;
        private String apiIdentifier;
        private ExactCurrencyAmount exactBalance;
        private String alias;
        private String productName;

        protected final void applyUniqueIdentifier(@Nonnull String uniqueIdentifier) {
            Preconditions.checkArgument(
                    !Strings.isNullOrEmpty(uniqueIdentifier),
                    "UniqueIdentifier must no be null or empty.");

            uniqueIdentifier = StringUtils.removeNonAlphaNumeric(uniqueIdentifier);

            Preconditions.checkArgument(
                    !Strings.isNullOrEmpty(uniqueIdentifier),
                    "UniqueIdentifier was empty after sanitation.");

            this.uniqueIdentifier = uniqueIdentifier;
        }

        protected final void applyAccountNumber(@Nonnull String accountNumber) {
            Preconditions.checkArgument(
                    !Strings.isNullOrEmpty(accountNumber),
                    "AccountNumber must not be null or empty.");

            this.accountNumber = accountNumber;
        }

        @Deprecated
        protected final void applyBalance(@Nonnull Amount balance) {
            Preconditions.checkNotNull(balance, "Balance must not be null.");

            this.exactBalance = ExactCurrencyAmount.of(balance.getValue(), balance.getCurrency());
        }

        protected final void applyBalance(@Nonnull ExactCurrencyAmount balance) {
            Preconditions.checkNotNull(balance, "Balance must not be null.");
            this.exactBalance = balance;
        }

        protected final void applyAlias(String alias) {
            this.alias = alias;
        }

        @Override
        public final B addAccountIdentifier(@Nonnull AccountIdentifier identifier) {
            Preconditions.checkNotNull(identifier, "AccountIdentifier must not be null.");

            if (identifiers.add(identifier)) {
                return buildStep();
            }

            throw new IllegalArgumentException(
                    String.format(
                            "Identifier %s is already present in the set.", identifier.getType()));
        }

        @Override
        public final B setApiIdentifier(@Nonnull String identifier) {
            this.apiIdentifier = identifier;
            return buildStep();
        }

        @Override
        public final B setProductName(@Nonnull String productName) {
            this.productName = productName;
            return buildStep();
        }

        @Override
        public final B addHolderName(@Nonnull String holderName) {
            this.holderNames.add(holderName);
            return buildStep();
        }

        @Override
        public final B addAccountFlags(@Nonnull AccountFlag... accountFlags) {
            this.accountFlags.addAll(Arrays.asList(accountFlags));
            return buildStep();
        }

        @Override
        public final <V> B putInTemporaryStorage(@Nonnull String key, @Nonnull V value) {
            this.temporaryStorage.put(key, value);
            return buildStep();
        }

        protected abstract B buildStep();

        String getUniqueIdentifier() {
            return uniqueIdentifier;
        }

        String getAccountNumber() {
            return accountNumber;
        }

        String getApiIdentifier() {
            return apiIdentifier;
        }

        @Deprecated
        Amount getBalance() {
            return new Amount(exactBalance.getCurrencyCode(), exactBalance.getDoubleValue());
        }

        ExactCurrencyAmount getExactBalance() {
            return exactBalance;
        }

        String getAlias() {
            return alias;
        }

        String getProductName() {
            return productName;
        }

        Collection<String> getHolderNames() {
            return holderNames;
        }

        Set<AccountIdentifier> getIdentifiers() {
            return identifiers;
        }

        TemporaryStorage getTemporaryStorage() {
            return temporaryStorage;
        }

        Set<AccountFlag> getAccountFlags() {
            return accountFlags;
        }
    }

    // This will be removed as part of the improved step builder + agent builder refactoring project

    /** @deprecated Use StepBuilder instead */
    @Deprecated
    public abstract static class Builder<A extends Account, T extends Builder<A, T>> {
        protected final List<AccountIdentifier> identifiers = Lists.newArrayList();
        protected final List<AccountFlag> accountFlags = Lists.newArrayList();
        protected final TemporaryStorage temporaryStorage = new TemporaryStorage();
        protected String name;
        protected String accountNumber;
        protected String uniqueIdentifier;
        protected HolderName holderName;
        protected ExactCurrencyAmount exactBalance;
        protected ExactCurrencyAmount exactAvailableCredit;
        private T thisObj;

        @Deprecated
        protected Builder(String uniqueIdentifier) {
            this.thisObj = self();

            Preconditions.checkArgument(
                    !Strings.isNullOrEmpty(uniqueIdentifier),
                    "Unique identifier is null or empty.");
            this.thisObj.uniqueIdentifier = uniqueIdentifier;
        }

        @Deprecated
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

        @Deprecated
        public Amount getBalance() {
            return Optional.ofNullable(thisObj.exactBalance)
                    .map(e -> new Amount(e.getCurrencyCode(), e.getDoubleValue()))
                    .orElseThrow(NullPointerException::new);
        }

        @Deprecated
        public T setBalance(Amount balance) {
            thisObj.exactBalance =
                    ExactCurrencyAmount.of(balance.getValue(), balance.getCurrency());
            return self();
        }

        public ExactCurrencyAmount getExactBalance() {
            return Optional.ofNullable(thisObj.exactBalance).orElseThrow(NullPointerException::new);
        }

        public T setExactBalance(ExactCurrencyAmount exactBalance) {
            this.exactBalance = exactBalance;
            return self();
        }

        public Amount getAvailableCredit() {
            return Optional.ofNullable(thisObj.exactAvailableCredit)
                    .map(e -> new Amount(e.getCurrencyCode(), e.getDoubleValue()))
                    .orElseThrow(NullPointerException::new);
        }

        public T setAvailableCredit(Amount availableCredit) {
            this.exactAvailableCredit =
                    ExactCurrencyAmount.of(
                            availableCredit.getValue(), availableCredit.getCurrency());
            return self();
        }

        public List<AccountIdentifier> getIdentifiers() {
            return thisObj.identifiers != null ? thisObj.identifiers : Collections.emptyList();
        }

        public T addIdentifier(AccountIdentifier identifier) {
            thisObj.identifiers.add(identifier);
            return self();
        }

        public T addIdentifiers(Collection<AccountIdentifier> identifiers) {
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

        public T addAccountFlags(Collection<AccountFlag> accountFlags) {
            thisObj.accountFlags.addAll(accountFlags);
            return self();
        }

        public String getUniqueIdentifier() {
            return Preconditions.checkNotNull(
                    thisObj.uniqueIdentifier, "Unique identifier must be set.");
        }

        public HolderName getHolderName() {
            return thisObj.holderName;
        }

        public T setHolderName(HolderName holderName) {
            thisObj.holderName = holderName;
            return self();
        }

        public <K> T putInTemporaryStorage(String key, K value) {
            temporaryStorage.put(key, value);
            return self();
        }

        private String getBankIdentifier() {
            return temporaryStorage.get(BANK_IDENTIFIER_KEY);
        }

        public T setBankIdentifier(String bankIdentifier) {
            temporaryStorage.put(BANK_IDENTIFIER_KEY, bankIdentifier);
            return self();
        }

        private TemporaryStorage getTransientStorage() {
            return temporaryStorage;
        }

        @Deprecated
        public abstract A build();

        public ExactCurrencyAmount getExactAvailableCredit() {
            return exactAvailableCredit;
        }

        public T setExactAvailableCredit(ExactCurrencyAmount exactAvailableCredit) {
            this.exactAvailableCredit = ExactCurrencyAmount.of(exactAvailableCredit);
            return self();
        }
    }
}
