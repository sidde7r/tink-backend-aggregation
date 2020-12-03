package se.tink.backend.agents.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.enums.AccountExclusion;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.GiroIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.TypeReferences;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Account implements Cloneable {
    private static final Predicate<AccountIdentifier> FIND_GIRO_WITH_OCR =
            accountIdentifier -> {
                if (accountIdentifier.is(Type.SE_BG) || accountIdentifier.is(Type.SE_PG)) {
                    Optional<String> ocr = accountIdentifier.to(GiroIdentifier.class).getOcr();
                    return ocr.isPresent();
                }

                return false;
            };
    private String accountNumber;
    private AccountExclusion accountExclusion;
    private Double availableCredit;
    private ExactCurrencyAmount exactAvailableCredit;
    private Double balance;
    private ExactCurrencyAmount exactBalance;
    private String currencyCode;
    private ExactCurrencyAmount availableBalance;
    private ExactCurrencyAmount creditLimit;
    private String bankId;
    private Date certainDate;
    private String credentialsId;
    private boolean excluded;
    private boolean favored;
    private String id;
    private String name;
    private double ownership;
    private String payload;
    private AccountTypes type;
    private String userId;
    private boolean userModifiedExcluded;
    private boolean userModifiedName;
    private boolean userModifiedType;
    private String identifiers;
    private List<TransferDestination> transferDestinations;
    private AccountDetails details;
    private boolean closed;
    private String holderName;
    private AccountHolder accountHolder;
    private String flags;
    private String financialInstitutionId;

    @JsonIgnore
    // Should not be mapped using
    // se.tink.backend.aggregation.agents.utils.mappers.CoreAccountMapper#fromAggregation
    private AccountCapabilities capabilities;

    @JsonIgnore
    // Should not be mapped using
    // se.tink.backend.aggregation.agents.utils.mappers.CoreAccountMapper#fromAggregation
    private AccountSourceInfo sourceInfo;

    private List<Balance> balances;

    public Account() {
        this.id = UUIDUtils.generateUUID();
        this.ownership = 1;
        this.identifiers = "[]";
        this.flags = "[]";
        this.accountExclusion = AccountExclusion.NONE;
        this.capabilities = AccountCapabilities.createDefault();
    }

    public ExactCurrencyAmount getExactAvailableCredit() {
        return exactAvailableCredit;
    }

    public void setExactAvailableCredit(ExactCurrencyAmount exactAvailableCredit) {
        this.exactAvailableCredit = exactAvailableCredit;
    }

    public ExactCurrencyAmount getExactBalance() {
        return exactBalance;
    }

    public void setExactBalance(ExactCurrencyAmount exactBalance) {
        this.exactBalance = exactBalance;
    }

    @Override
    public Account clone() throws CloneNotSupportedException {
        return (Account) super.clone();
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    /**
     * Returns availableCredit for the account. Translating a null balance to zero.
     *
     * @return availableCredit of the account
     */
    @JsonProperty("availableCredit")
    public double getAvailableCredit() {
        return this.availableCredit == null ? 0.0d : this.availableCredit;
    }

    /**
     * Returns availableCredit for the account. Value may be null.
     *
     * @return availableCredit of the account
     */
    public Double getNullableAvailableCredit() {
        return this.availableCredit;
    }

    public void setAvailableCredit(Double availableCredit) {
        this.availableCredit = availableCredit;
    }

    /**
     * Returns balance for the account. Translating a null balance to zero.
     *
     * @return balance of the account
     */
    @JsonProperty("balance")
    public double getBalance() {
        return this.balance == null ? 0.0d : this.balance;
    }

    /**
     * Returns balance for the account. Value may be null.
     *
     * @return balance of the account
     */
    public Double getNullableBalance() {
        return this.balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    /**
     * @return Unique identifier on the client side, not to be confused with nxgen
     *     Account.getBankIdentifier
     */
    public String getBankId() {
        return this.bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public Date getCertainDate() {
        return this.certainDate;
    }

    public void setCertainDate(Date certainDate) {
        this.certainDate = certainDate;
    }

    public String getCredentialsId() {
        return this.credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Forces serialization to use this getter in order to ensure account name fallback similar to
    // that
    // existing in nxgen account builders. This solves issues in legacy agents where name is not
    // set.
    @JsonProperty("name")
    public String getNameWithFallback() {
        return this.name != null ? this.name : getAccountNumber();
    }

    public double getOwnership() {
        return this.ownership;
    }

    public void setOwnership(double ownership) {
        this.ownership = ownership;
    }

    public String getPayload() {
        return this.payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public AccountTypes getType() {
        return this.type;
    }

    public void setType(AccountTypes type) {
        this.type = type;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String user) {
        this.userId = user;
    }

    public boolean isExcluded() {
        return this.excluded;
    }

    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
    }

    public boolean isFavored() {
        return this.favored;
    }

    public void setFavored(boolean favored) {
        this.favored = favored;
    }

    public boolean isUserModifiedName() {
        return this.userModifiedName;
    }

    public void setUserModifiedName(boolean userModifiedName) {
        this.userModifiedName = userModifiedName;
    }

    public boolean isUserModifiedType() {
        return this.userModifiedType;
    }

    public void setUserModifiedType(boolean userModifiedType) {
        this.userModifiedType = userModifiedType;
    }

    public String getHolderName() {
        return this.holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public List<AccountFlag> getFlags() {
        return deserializeFlags();
    }

    public void setFlags(Collection<AccountFlag> flags) {
        for (AccountFlag flag : flags) {
            this.putFlag(flag);
        }
    }

    public String getFinancialInstitutionId() {
        return financialInstitutionId;
    }

    public void setFinancialInstitutionId(String financialInstitutionId) {
        this.financialInstitutionId = financialInstitutionId;
    }

    public List<TransferDestination> getTransferDestinations() {
        return this.transferDestinations;
    }

    public void setTransferDestinations(List<TransferDestination> transferDestinations) {
        this.transferDestinations = transferDestinations;
    }

    public boolean isClosed() {
        return this.closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public void putFlag(AccountFlag flag) {
        Set<String> flags = Sets.newHashSet();
        if (this.flags != null) {
            flags =
                    Sets.newHashSet(
                            SerializationUtils.deserializeFromString(
                                    this.flags, TypeReferences.LIST_OF_STRINGS));
        }

        flags.add(flag.name());
        this.flags = SerializationUtils.serializeToString(Lists.newArrayList(flags));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("id", getId())
                .add("credentialsid", getCredentialsId())
                .add("userid", getUserId())
                .toString();
    }

    public List<AccountIdentifier> getIdentifiers() {
        return deserializeIdentifiers();
    }

    public void setIdentifiers(Collection<AccountIdentifier> identifiers) {
        for (AccountIdentifier identifier : identifiers) {
            this.putIdentifier(identifier);
        }
    }

    public String getIdentifiersSerialized() {
        return this.identifiers;
    }

    @JsonIgnore
    public AccountIdentifier getPreferredIdentifier(Type destinationIdentifierType) {
        switch (destinationIdentifierType) {
            case SE:
            case SE_PG:
            case SE_BG:
            case SE_SHB_INTERNAL:
            case TINK:
                return getIdentifier(Type.SE);
            case FI:
            case IBAN:
                return getIdentifier(Type.IBAN);
            case BE:
                return getIdentifier(Type.BE);
            case SEPA_EUR:
                return getIdentifier(Type.SEPA_EUR);
        }
        return null;
    }

    public void putIdentifier(AccountIdentifier identifier) {
        if (identifier == null || !identifier.isValid()) {
            return;
        }

        Set<String> ids = Sets.newHashSet();
        if (this.identifiers != null) {
            ids =
                    Sets.newHashSet(
                            SerializationUtils.deserializeFromString(
                                    this.identifiers, TypeReferences.LIST_OF_STRINGS));
        }
        ids.add(identifier.toUriAsString());

        this.identifiers = SerializationUtils.serializeToString(Lists.newArrayList(ids));
    }

    @JsonIgnore
    public AccountIdentifier getIdentifier(AccountIdentifier.Type type) {
        if (this.identifiers == null) {
            return null;
        }
        List<AccountIdentifier> identifiers = deserializeIdentifiers();

        Optional<AccountIdentifier> identifier =
                identifiers.stream()
                        .filter(identifier1 -> identifier1.getType() == type)
                        .findFirst();

        // If we couldn't find a matching Identifier, try find a PG/BG with a predefined OCR
        return identifier.orElseGet(
                () -> identifiers.stream().filter(FIND_GIRO_WITH_OCR).findFirst().orElse(null));
    }

    @JsonIgnore
    public <T extends AccountIdentifier> T getIdentifier(
            AccountIdentifier.Type type, Class<T> cls) {
        AccountIdentifier identifier = getIdentifier(type);
        if (identifier == null) {
            return null;
        }
        return cls.cast(identifier);
    }

    private List<AccountIdentifier> deserializeIdentifiers() {
        List<AccountIdentifier> accountIdentifiers = Lists.newArrayList();

        if (this.identifiers != null) {
            List<String> ids =
                    SerializationUtils.deserializeFromString(
                            this.identifiers, TypeReferences.LIST_OF_STRINGS);
            for (String id : ids) {
                accountIdentifiers.add(AccountIdentifier.create(URI.create(id)));
            }
        }
        return accountIdentifiers;
    }

    public boolean definedBy(AccountIdentifier identifier) {
        if (identifier.getType() == AccountIdentifier.Type.TINK) {
            return getId().equals(identifier.getIdentifier());
        }

        if (getIdentifiers() == null) {
            return false;
        }

        for (AccountIdentifier id : getIdentifiers()) {
            if (identifier.equals(id)) {
                return true;
            }
        }

        return false;
    }

    private List<AccountFlag> deserializeFlags() {
        List<AccountFlag> accountFlags = Lists.newArrayList();

        if (this.flags != null) {
            List<String> flags =
                    SerializationUtils.deserializeFromString(
                            this.flags, TypeReferences.LIST_OF_STRINGS);
            for (String flag : flags) {
                accountFlags.add(AccountFlag.valueOf(flag));
            }
        }
        return accountFlags;
    }

    public AccountDetails getDetails() {
        return this.details;
    }

    public void setDetails(AccountDetails details) {
        this.details = details;
    }

    /**
     * Means that exclusion of the account is controlled by user and shouldn't be changed
     * automatically.
     *
     * @return true if account exclusion is user controlled
     */
    public boolean isUserModifiedExcluded() {
        return this.userModifiedExcluded;
    }

    public void setUserModifiedExcluded(boolean userModifiedExcluded) {
        this.userModifiedExcluded = userModifiedExcluded;
    }

    @JsonIgnore
    public String getPayload(String key) {
        Map<String, String> map = getPayloadAsMap();

        return map == null ? null : map.get(key);
    }

    @JsonIgnore
    public void putPayload(String key, String value) {
        Map<String, String> map = getPayloadAsMap();

        if (map == null) {
            map = Maps.newHashMap();
        }

        map.put(key, value);

        this.payload = SerializationUtils.serializeToString(map);
    }

    @JsonIgnore
    public void removePayload(String key) {
        Map<String, String> map = getPayloadAsMap();

        if (map == null) {
            return;
        }

        map.remove(key);

        this.payload = SerializationUtils.serializeToString(map);
    }

    @JsonIgnore
    public Map<String, String> getPayloadAsMap() {

        if (Strings.isNullOrEmpty(this.payload)) {
            return Maps.newHashMap();
        } else {
            // Will return null if it wasn't possible to convert the payload to a map
            return SerializationUtils.deserializeFromString(
                    this.payload, TypeReferences.MAP_OF_STRING_STRING);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(bankId, account.bankId);
    }

    public static boolean deepEquals(Account first, Account second) {
        if (first == null || second == null) {
            return false;
        }

        if (first == second) {
            return true;
        }

        return Double.compare(first.availableCredit, second.availableCredit) == 0
                && Double.compare(first.balance, second.balance) == 0
                && first.excluded == second.excluded
                && first.favored == second.favored
                && Double.compare(first.ownership, second.ownership) == 0
                && first.userModifiedExcluded == second.userModifiedExcluded
                && first.userModifiedName == second.userModifiedName
                && first.userModifiedType == second.userModifiedType
                && first.closed == second.closed
                && Objects.equals(first.accountNumber, second.accountNumber)
                && first.accountExclusion == second.accountExclusion
                && Objects.equals(first.exactAvailableCredit, second.exactAvailableCredit)
                && Objects.equals(first.exactBalance, second.exactBalance)
                && Objects.equals(first.currencyCode, second.currencyCode)
                && Objects.equals(first.creditLimit, second.creditLimit)
                && Objects.equals(first.availableBalance, second.availableBalance)
                && Objects.equals(first.bankId, second.bankId)
                && Objects.equals(first.certainDate, second.certainDate)
                && Objects.equals(first.credentialsId, second.credentialsId)
                && Objects.equals(first.name, second.name)
                && Objects.equals(first.payload, second.payload)
                && first.type == second.type
                && Objects.equals(first.userId, second.userId)
                && Objects.equals(first.identifiers, second.identifiers)
                && Objects.equals(first.details, second.details)
                && Objects.equals(first.holderName, second.holderName)
                && Objects.equals(first.flags, second.flags)
                && Objects.equals(first.accountHolder, second.accountHolder)
                && Objects.equals(first.financialInstitutionId, second.financialInstitutionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bankId);
    }

    public AccountExclusion getAccountExclusion() {
        return accountExclusion;
    }

    public void setAccountExclusion(AccountExclusion accountExclusion) {
        this.accountExclusion = accountExclusion;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public ExactCurrencyAmount getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(ExactCurrencyAmount availableBalance) {
        this.availableBalance = availableBalance;
    }

    public ExactCurrencyAmount getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(ExactCurrencyAmount creditLimit) {
        this.creditLimit = creditLimit;
    }

    public AccountCapabilities getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(AccountCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    public AccountSourceInfo getSourceInfo() {
        return sourceInfo;
    }

    public void setSourceInfo(AccountSourceInfo sourceInfo) {
        this.sourceInfo = sourceInfo;
    }

    public AccountHolder getAccountHolder() {
        return accountHolder;
    }

    public void setAccountHolder(AccountHolder accountHolder) {
        this.accountHolder = accountHolder;
    }

    public List<Balance> getBalances() {
        return balances;
    }

    public void setBalances(List<Balance> balances) {
        this.balances = balances;
    }
}
