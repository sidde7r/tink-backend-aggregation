package se.tink.backend.aggregation.nxgen.core.account;

import static se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role.HOLDER;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountHolder;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.HolderIdentity;
import se.tink.backend.agents.rpc.HolderRole;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.utils.typeguesser.accountholder.AccountHolderTypeUtil;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.BuildStep;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
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
    protected IdModule idModule;
    protected String name;
    protected String productName;
    protected String accountNumber;
    protected Set<AccountIdentifier> identifiers;
    protected String uniqueIdentifier;
    protected String apiIdentifier;
    protected TemporaryStorage temporaryStorage;
    protected Set<AccountFlag> accountFlags;
    protected ExactCurrencyAmount exactBalance;
    protected ExactCurrencyAmount exactAvailableBalance;
    protected ExactCurrencyAmount exactAvailableCredit;
    protected ExactCurrencyAmount exactCreditLimit;
    protected Map<String, String> payload;
    protected AccountCapabilities capabilities;
    protected AccountSourceInfo sourceInfo;
    protected final List<Party> parties;
    protected AccountHolderType holderType;
    protected List<Balance> balances;

    protected Account(AccountBuilder<? extends Account, ?> builder, BalanceModule balanceModule) {
        this(
                builder,
                balanceModule.getExactBalance(),
                balanceModule.getExactAvailableCredit().orElse(null));
        this.exactAvailableBalance = balanceModule.getExactAvailableBalance();
        this.exactCreditLimit = balanceModule.getExactCreditLimit();
    }
    // Exists for interoperability only, do not ever use
    protected Account(
            AccountBuilder<? extends Account, ?> builder,
            ExactCurrencyAmount balance,
            ExactCurrencyAmount availableCredit) {
        // These exist for interoperability and will eventually be removed
        this.name = builder.getIdModule().getAccountName();
        this.accountNumber = builder.getIdModule().getAccountNumber();
        this.exactAvailableCredit = availableCredit;
        this.exactBalance = balance;
        this.identifiers = builder.getIdModule().getIdentifiers();
        this.uniqueIdentifier = builder.getIdModule().getUniqueId();
        this.idModule = builder.getIdModule();
        this.apiIdentifier = builder.getApiIdentifier();
        this.parties = builder.getParties();
        this.holderType = builder.getHolderType();
        this.temporaryStorage = builder.getTransientStorage();
        this.accountFlags = ImmutableSet.copyOf(builder.getAccountFlags());
        this.payload = builder.getPayload();
        this.capabilities = builder.getCapabilities();
        this.sourceInfo = builder.getSourceInfo();
        this.balances = builder.getBalances();
    }

    // This will be removed as part of the improved step builder + agent builder refactoring project
    @Deprecated
    protected Account(Builder<? extends Account, ? extends Account.Builder> builder) {
        this.name = builder.getName();
        this.accountNumber = builder.getAccountNumber();
        this.identifiers = ImmutableSet.copyOf(builder.getIdentifiers());
        this.uniqueIdentifier = sanitizeUniqueIdentifier(builder.getUniqueIdentifier());
        this.apiIdentifier = builder.getBankIdentifier();
        this.parties =
                Optional.ofNullable(builder.getHolderName())
                        .filter(holderName -> !Strings.isNullOrEmpty(holderName.toString()))
                        .map(
                                holderName ->
                                        ImmutableList.of(new Party(holderName.toString(), HOLDER)))
                        .orElse(ImmutableList.of());
        this.temporaryStorage = builder.getTransientStorage();
        this.accountFlags = ImmutableSet.copyOf(builder.getAccountFlags());
        this.exactBalance = builder.getExactBalance();
        this.exactAvailableCredit =
                Optional.ofNullable(builder.getExactAvailableCredit()).orElse(null);
        this.payload = Maps.newHashMap();
        this.capabilities = builder.getCapabilities();
        this.sourceInfo = builder.getSourceInfo();
        // Safe-guard against uniqueIdentifiers containing only formatting characters (e.g. '*' or
        // '-').
        Preconditions.checkState(
                !Strings.isNullOrEmpty(uniqueIdentifier),
                "Unique identifier was empty after sanitation.");
        // USE NEXTGEN BUILDER
        this.balances = new ArrayList<>();
    }

    protected Account(StepBuilder<? extends Account, ?> builder) {
        this.accountNumber = builder.getAccountNumber();
        this.apiIdentifier = builder.getApiIdentifier();
        this.exactBalance = builder.getExactBalance();
        this.identifiers = ImmutableSet.copyOf(builder.getIdentifiers());
        this.uniqueIdentifier = builder.getUniqueIdentifier();
        this.temporaryStorage = builder.getTemporaryStorage();
        this.accountFlags = ImmutableSet.copyOf(builder.getAccountFlags());
        this.productName = builder.getProductName();
        this.payload = Maps.newHashMap();
        this.capabilities = builder.getCapabilities();
        this.sourceInfo = builder.getSourceInfo();

        if (Strings.isNullOrEmpty(builder.getAlias())) {
            // Fallback in case the received alias happened to be null at run-time.
            // Indicates a programming fault because the agent should be implemented in a way such
            // that it always sets the alias to the displayed name of the account.
            logger.error("Supplied alias was null -- falling back to account number");
            this.name = builder.getAccountNumber();
        } else {
            this.name = builder.getAlias();
        }

        this.parties =
                Optional.ofNullable(builder.getHolderNames()).orElse(Collections.emptyList())
                        .stream()
                        .filter(holderName -> !Strings.isNullOrEmpty(holderName))
                        .map(holderName -> new Party(holderName, HOLDER))
                        .collect(Collectors.toList());
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

    public ExactCurrencyAmount getExactBalance() {
        return exactBalance;
    }

    public IdModule getIdModule() {
        return idModule;
    }

    public ExactCurrencyAmount getExactAvailableCredit() {
        return exactAvailableCredit;
    }

    public ExactCurrencyAmount getExactAvailableBalance() {
        return exactAvailableBalance;
    }

    public ExactCurrencyAmount getExactCreditLimit() {
        return exactCreditLimit;
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

    /** @return Unique identifier on the bank side, not to be confused with rpc Account.getBankId */
    public String getApiIdentifier() {
        return this.apiIdentifier;
    }

    public Map<String, String> getPayload() {
        return payload;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Account && Objects.equal(hashCode(), obj.hashCode());
    }

    @Override
    public int hashCode() {
        return getUniqueIdentifier().hashCode();
    }

    public se.tink.backend.agents.rpc.Account toSystemAccount(User user, Provider provider) {
        se.tink.backend.agents.rpc.Account account = new se.tink.backend.agents.rpc.Account();

        account.setType(getType());
        account.setName(this.name);
        account.setAccountNumber(this.accountNumber);
        account.setBalance(this.exactBalance.getDoubleValue());
        account.setCurrencyCode(this.exactBalance.getCurrencyCode());
        account.setExactBalance(this.exactBalance);
        account.setIdentifiers(this.identifiers);
        account.setBankId(this.uniqueIdentifier);
        account.setHolderName(getFirstHolder().orElse(null));
        account.setFlags(this.accountFlags);
        account.setPayload(createPayload(user));
        account.setAvailableCredit(
                Optional.ofNullable(this.exactAvailableCredit)
                        .map(ExactCurrencyAmount::getDoubleValue)
                        .orElse(0.0));
        account.setExactAvailableCredit(this.exactAvailableCredit);
        account.setAvailableBalance(this.exactAvailableBalance);
        account.setCreditLimit(this.exactCreditLimit);
        account.setCapabilities(this.capabilities);
        account.setSourceInfo(this.sourceInfo);

        if (parties.size() > 0 || java.util.Objects.nonNull(holderType)) {
            AccountHolder accountHolder = new AccountHolder();
            accountHolder.setType(
                    Optional.ofNullable(holderType)
                            .orElse(inferHolderType(provider))
                            .toSystemType());
            accountHolder.setIdentities(
                    parties.stream().map(this::toSystemHolder).collect(Collectors.toList()));
            account.setAccountHolder(accountHolder);
        }

        account.setBalances(
                balances != null
                        ? balances.stream()
                                .map(Balance::toSystemBalance)
                                .collect(Collectors.toList())
                        : ImmutableList.of());
        return account;
    }

    public HolderIdentity toSystemHolder(Party party) {
        HolderIdentity systemHolder = new HolderIdentity();
        systemHolder.setName(party.getName());
        systemHolder.setRole(toSystemRole(party.getRole()));
        return systemHolder;
    }

    private HolderRole toSystemRole(Party.Role partyRole) {
        switch (partyRole) {
            case HOLDER:
                return HolderRole.HOLDER;
            case AUTHORIZED_USER:
                return HolderRole.AUTHORIZED_USER;
            case OTHER:
                return HolderRole.OTHER;
            default:
                return null;
        }
    }

    private AccountHolderType inferHolderType(Provider provider) {
        String accountHolderTypeAsString = AccountHolderTypeUtil.inferHolderType(provider).name();
        return AccountHolderType.valueOf(accountHolderTypeAsString);
    }

    private Optional<String> getFirstHolder() {
        return parties.stream()
                .filter(party -> HOLDER.equals(party.getRole()))
                .findFirst()
                .map(Party::getName);
    }

    public HolderName getHolderName() {
        return getFirstHolder().map(HolderName::new).orElse(null);
    }

    public List<Party> getParties() {
        return parties;
    }

    public AccountHolderType getHolderType() {
        return holderType;
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
        if (FeatureFlags.FeatureFlagGroup.MULTI_CURRENCY_FOR_POCS.isFlagInGroup(user.getFlags())) {
            payload.put("currency", exactBalance.getCurrencyCode());
        }

        if (payload.isEmpty()) {
            return null;
        }

        return SerializationUtils.serializeToString(payload);
    }

    public List<Balance> getBalances() {
        return balances;
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
        private AccountCapabilities capabilities = AccountCapabilities.createDefault();
        private AccountSourceInfo sourceInfo;

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

            this.exactBalance =
                    ExactCurrencyAmount.of(balance.toBigDecimal(), balance.getCurrency());
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

        @Override
        public B canWithdrawCash(AccountCapabilities.Answer canWithdrawCash) {
            this.capabilities.setCanWithdrawCash(canWithdrawCash);
            return buildStep();
        }

        @Override
        public B canPlaceFunds(AccountCapabilities.Answer canPlaceFunds) {
            this.capabilities.setCanPlaceFunds(canPlaceFunds);
            return buildStep();
        }

        @Override
        public B canExecuteExternalTransfer(AccountCapabilities.Answer canExecuteExternalTransfer) {
            this.capabilities.setCanExecuteExternalTransfer(canExecuteExternalTransfer);
            return buildStep();
        }

        @Override
        public B canReceiveExternalTransfer(AccountCapabilities.Answer canReceiveExternalTransfer) {
            this.capabilities.setCanReceiveExternalTransfer(canReceiveExternalTransfer);
            return buildStep();
        }

        @Override
        public B sourceInfo(AccountSourceInfo sourceInfo) {
            this.sourceInfo = sourceInfo;
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

        public AccountCapabilities getCapabilities() {
            return capabilities;
        }

        public AccountSourceInfo getSourceInfo() {
            return sourceInfo;
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
        private AccountCapabilities capabilities = AccountCapabilities.createDefault();
        private AccountSourceInfo sourceInfo;
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

        public ExactCurrencyAmount getExactBalance() {
            return Optional.ofNullable(thisObj.exactBalance).orElseThrow(NullPointerException::new);
        }

        public T setExactBalance(ExactCurrencyAmount exactBalance) {
            this.exactBalance = exactBalance;
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
            this.exactAvailableCredit = exactAvailableCredit;
            return self();
        }

        public T canWithdrawCash(AccountCapabilities.Answer canWithdrawCash) {
            this.capabilities.setCanWithdrawCash(canWithdrawCash);
            return self();
        }

        public T canPlaceFunds(AccountCapabilities.Answer canPlaceFunds) {
            this.capabilities.setCanPlaceFunds(canPlaceFunds);
            return self();
        }

        public T canExecuteExternalTransfer(AccountCapabilities.Answer canExecuteExternalTransfer) {
            this.capabilities.setCanExecuteExternalTransfer(canExecuteExternalTransfer);
            return self();
        }

        public T canReceiveExternalTransfer(AccountCapabilities.Answer canReceiveExternalTransfer) {
            this.capabilities.setCanReceiveExternalTransfer(canReceiveExternalTransfer);
            return self();
        }

        public T sourceInfo(AccountSourceInfo sourceInfo) {
            this.sourceInfo = sourceInfo;
            return self();
        }

        public AccountCapabilities getCapabilities() {
            return capabilities;
        }

        public AccountSourceInfo getSourceInfo() {
            return sourceInfo;
        }
    }
}
