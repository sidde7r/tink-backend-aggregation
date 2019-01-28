package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.protostuff.Exclude;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import se.tink.libraries.enums.AccountFlag;
import se.tink.libraries.enums.AccountExclusion;
import se.tink.backend.core.transfer.TransferDestination;
import se.tink.libraries.serialization.TypeReferences;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.GiroIdentifier;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.strings.StringUtils;

@Entity
@Table(name = "accounts")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Account implements Cloneable {
    @Tag(1)
    @ApiModelProperty(name = "accountNumber", value="The account number of the account. Not necessarily always a full clearingnumber-accountnumber. It can be formatted differently for different accounts and banks.", example = "1234-123456789", required = true)
    private String accountNumber;
    @Exclude
    @ApiModelProperty(name = "availableCredit", hidden = true)
    private double availableCredit;
    @Tag(2)
    @ApiModelProperty(name = "balance", value="The current balance of the account.", example = "34567.50", required = true)
    private double balance;
    @Exclude
    @ApiModelProperty(name = "bankId", hidden = true)
    private String bankId;
    @Exclude
    @ApiModelProperty(name = "certainDate", hidden = true)
    private Date certainDate;
    @Tag(3)
    @ApiModelProperty(name = "credentialsId", value="The internal identifier of the credentials that the account belongs to.", example = "6e68cc6287704273984567b3300c5822", required = true)
    private String credentialsId;
    @Tag(4)
    @ApiModelProperty(name = "excluded", value="Indicates if the user has excluded the account. This can be modified by the user.", example = "false", required = true)
    private boolean excluded;
    @Tag(5)
    @ApiModelProperty(name = "favored", value="Indicates if the user has favored the account. This can be modified by the user.", example = "false", required = true)
    private boolean favored;
    @Id
    @Tag(6)
    @ApiModelProperty(name = "id", value="The internal identifier of account.", example = "a6bb87e57a8c4dd4874b241471a2b9e8", required = true)
    private String id;
    @Tag(7)
    @ApiModelProperty(name = "name", value="The display name of the account. This can be modified by the user.", example = "Privatkonto", required = true)
    private String name;
    @Tag(8)
    @ApiModelProperty(name = "ownership", value="The ownership ratio indicating how much of the account is owned by the user. This is used to determine how much of transactions belonging to this account should be attributed to the user when statistics are calculated. This can be modified by the user.", example = "0.5", required = true)
    private double ownership;
    @Exclude
    @ApiModelProperty(name = "payload", hidden = true)
    private String payload;
    @Enumerated(EnumType.STRING)
    @Modifiable
    @Tag(9)
    @ApiModelProperty(name = "type", value = "The type of the account. This can be modified by the user.", required = true, allowableValues = AccountTypes.DOCUMENTED)
    private AccountTypes type;
    @Exclude
    @ApiModelProperty(name = "userId", hidden = true)
    private String userId;
    @Exclude
    @ApiModelProperty(name = "userModifiedExcluded", hidden = true)
    private boolean userModifiedExcluded;
    @Exclude
    @ApiModelProperty(name = "userModifiedName", hidden = true)
    private boolean userModifiedName;
    @Exclude
    @ApiModelProperty(name = "userModifiedType", hidden = true)
    private boolean userModifiedType;
    @Tag(10)
    @ApiModelProperty(name = "identifiers", value="All possible ways to uniquely identify this Account. An se-identifier is built up like: se://{clearingnumber}{accountnumber}", example = "[\"se://9999111111111111\"]")
    private String identifiers;
    @Tag(11)
    @Transient
    @ApiModelProperty(name = "transferDestinations", value="This field contains all the destinations this Account can transfer money to, be that payment or bank transfer recipients. It will only be populated if getting accounts via GET /transfer/accounts (i.e. not through GET /accounts).")
    private List<TransferDestination> transferDestinations;
    // Tag 12 can never be used again because nobody knows if something will break https://developers.google
    // .com/protocol-buffers/docs/proto
    @Tag(13)
    @Transient
    @ApiModelProperty(name = "details", value="If available, details are populated.")
    private AccountDetails details;
    @Tag(15)
    @ApiModelProperty(name = "holderName", value="The name of the account holder", example = "Thomas Alan Waits")
    private String holderName;
    @Exclude
    private boolean closed;
    @Tag(16)
    @ApiModelProperty(name = "flags", value="A list of flags specifying attributes on an account", example = "[\"MANDATE\"]", allowableValues = AccountFlag.DOCUMENTED)
    private String flags;
    @Tag(17)
    @ApiModelProperty(name = "accountExclusion", value = "The type of account exclusion. This can be modified by the user.", required = true, allowableValues = AccountExclusion.DOCUMENTED)
    private AccountExclusion accountExclusion;

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

    public AccountExclusion getAccountExclusion() {
        return accountExclusion;
    }

    public void setAccountExclusion(AccountExclusion accountExclusion) {
        this.accountExclusion = accountExclusion;
    }

    public static class PayloadKeys {
        public static final String PARTNER_PAYLOAD = "PARTNER_PAYLOAD";
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

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public boolean isClosed() {
        return this.closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public List<AccountFlag> getFlags() {
        return deserializeFlags();
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

    @Transient
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
        if (!identifier.isValid()) {
            return;
        }

        Set<String> ids = Sets.newHashSet();
        if (this.identifiers != null) {
            ids = Sets.newHashSet(SerializationUtils.deserializeFromString(this.identifiers, TypeReferences.LIST_OF_STRINGS));
        }
        ids.add(identifier.toUriAsString());

        this.identifiers = SerializationUtils.serializeToString(Lists.newArrayList(ids));
    }

    @Transient
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
    @Transient
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
            for(String id : ids) {
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

    public AccountDetails getDetails() {
        return this.details;
    }

    public void setDetails(AccountDetails details) {
        this.details = details;
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
    @Transient
    public String getPayload(String key) {
        Map<String, String> map = getPayloadAsMap();

        return map == null ? null : map.get(key);
    }

    @JsonIgnore
    @Transient
    public void putPayload(String key, String value) {
        Map<String, String> map = getPayloadAsMap();

        if (map == null) {
            map = Maps.newHashMap();
        }

        map.put(key, value);

        this.payload = SerializationUtils.serializeToString(map);
    }

    @JsonIgnore
    @Transient
    public void removePayload(String key) {
        Map<String, String> map = getPayloadAsMap();

        if (map == null) {
            return;
        }

        map.remove(key);

        this.payload = SerializationUtils.serializeToString(map);
    }

    @JsonIgnore
    @Transient
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
}
