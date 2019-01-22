package se.tink.backend.aggregation.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import se.tink.backend.core.AccountFlag;
import se.tink.backend.core.enums.AccountExclusion;
import se.tink.backend.core.transfer.TransferDestination;
import se.tink.libraries.serialization.TypeReferences;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.GiroIdentifier;
import se.tink.libraries.serialization.utils.SerializationUtils;

/**
 * Today two way mapping between Aggregation's Account and core account forces
 * the classes to have exactly the same fields. Can be cleaned up when the two
 * way dependency is resolved. An example of logic that breaks:
 *
 * {@link se.tink.backend.aggregation.workers.AgentWorkerContext#updateAccount(Account, AccountAssets)}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Account implements Cloneable {
    private String accountNumber;
    private AccountExclusion accountExclusion;
    private double availableCredit;
    private double balance;
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
    private ImageUrls images;
    private AccountDetails details;
    private boolean closed;
    private String holderName;
    private String flags;

    @Override
    public Account clone() throws CloneNotSupportedException {
        return (Account) super.clone();
    }

    public Account() {
        this.id = StringUtils.generateUUID();
        this.ownership = 1;
        this.identifiers = "[]";
        this.flags = "[]";
        this.accountExclusion = AccountExclusion.NONE;
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    public double getAvailableCredit() {
        return this.availableCredit;
    }

    public double getBalance() {
        return this.balance;
    }

    /**
     * @return Unique identifier on the client side, not to be confused with nxgen Account.getBankIdentifier
     */
    public String getBankId() {
        return this.bankId;
    }

    public Date getCertainDate() {
        return this.certainDate;
    }

    public String getCredentialsId() {
        return this.credentialsId;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public double getOwnership() {
        return this.ownership;
    }

    public String getPayload() {
        return this.payload;
    }

    public AccountTypes getType() {
        return this.type;
    }

    public String getUserId() {
        return this.userId;
    }

    public boolean isExcluded() {
        return this.excluded;
    }

    public boolean isFavored() {
        return this.favored;
    }

    public boolean isUserModifiedName() {
        return this.userModifiedName;
    }

    public boolean isUserModifiedType() {
        return this.userModifiedType;
    }

    public String getHolderName() {
        return this.holderName;
    }

    public List<AccountFlag> getFlags() {
        return deserializeFlags();
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setAvailableCredit(double availableCredit) {
        this.availableCredit = availableCredit;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public void setCertainDate(Date certainDate) {
        this.certainDate = certainDate;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
    }

    public void setFavored(boolean favored) {
        this.favored = favored;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOwnership(double ownership) {
        this.ownership = ownership;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setType(AccountTypes type) {
        this.type = type;
    }

    public void setUserId(String user) {
        this.userId = user;
    }

    public void setUserModifiedName(boolean userModifiedName) {
        this.userModifiedName = userModifiedName;
    }

    public void setUserModifiedType(boolean userModifiedType) {
        this.userModifiedType = userModifiedType;
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

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public void setFlags(List<AccountFlag> flags) {
        for (AccountFlag flag: flags) {
            this.putFlag(flag);
        }
    }

    public void putFlag(AccountFlag flag) {
        Set<String> flags = Sets.newHashSet();
        if (this.flags != null) {
            flags = Sets.newHashSet(SerializationUtils.deserializeFromString(this.flags, TypeReferences.LIST_OF_STRINGS));
        }

        flags.add(flag.name());
        this.flags = SerializationUtils.serializeToString(Lists.newArrayList(flags));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass()).add("id", getId()).add("credentialsid", getCredentialsId())
                .add("userid", getUserId()).toString();
    }

    public List<AccountIdentifier> getIdentifiers() {
        return deserializeIdentifiers();
    }

    public void setIdentifiers(List<AccountIdentifier> identifiers) {
        for (AccountIdentifier identifier : identifiers) {
            this.putIdentifier(identifier);
        }
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
            ids = Sets
                    .newHashSet(SerializationUtils.deserializeFromString(this.identifiers, TypeReferences.LIST_OF_STRINGS));
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

        Optional<AccountIdentifier> identifier = identifiers.stream()
                .filter(identifier1 -> identifier1.getType() == type).findFirst();

        // If we couldn't find a matching Identifier, try find a PG/BG with a predefined OCR
        return identifier.orElseGet(() -> identifiers.stream().filter(FIND_GIRO_WITH_OCR).findFirst().orElse(null));
    }

    @JsonIgnore
    public <T extends AccountIdentifier> T getIdentifier(AccountIdentifier.Type type, Class<T> cls) {
        AccountIdentifier identifier = getIdentifier(type);
        if (identifier == null) {
            return null;
        }
        return cls.cast(identifier);
    }

    private List<AccountIdentifier> deserializeIdentifiers() {
        List<AccountIdentifier> accountIdentifiers = Lists.newArrayList();

        if (this.identifiers != null) {
            List<String> ids = SerializationUtils.deserializeFromString(this.identifiers, TypeReferences.LIST_OF_STRINGS);
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
            List<String> flags = SerializationUtils.deserializeFromString(this.flags, TypeReferences.LIST_OF_STRINGS);
            for(String flag : flags) {
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

    public ImageUrls getImages() {
        return this.images;
    }

    public void setImages(ImageUrls images) {
        this.images = images;
    }

    /**
     * Means that exclusion of the account is controlled by user and shouldn't
     * be changed automatically.
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
            return SerializationUtils.deserializeFromString(this.payload, TypeReferences.MAP_OF_STRING_STRING);
        }
    }

    private static final Predicate<AccountIdentifier> FIND_GIRO_WITH_OCR = accountIdentifier -> {
        if (accountIdentifier.is(Type.SE_BG) || accountIdentifier.is(Type.SE_PG)) {
            Optional<String> ocr = accountIdentifier.to(GiroIdentifier.class).getOcr();
            return ocr.isPresent();
        }

        return false;
    };

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Account account = (Account) o;
        return Objects.equals(bankId, account.bankId);
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
}
