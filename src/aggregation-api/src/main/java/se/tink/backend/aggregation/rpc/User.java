package se.tink.backend.aggregation.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import se.tink.backend.core.UserConnectedService;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

/**
 * Aggregation RPC User
 *
 * {@link se.tink.backend.core.User} objects (from main-api) can be converted to User objects with
 */
public class User {
    private static final TypeReference<List<String>> STRING_LIST_TYPE_REFERENCE = new TypeReference<List<String>>() {
    };

    @JsonIgnore
    private boolean blocked;
    @JsonIgnore
    private boolean confirmed;
    private Date created;
    private String endpoint;
    private List<String> flags;
    private String flagsSerialized;
    @JsonIgnore
    private String hash;
    private String id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String password;
    private UserProfile profile;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<UserConnectedService> services;
    private String username;
    private Date debugUntil;
    private String nationalId;

    public User() {
        id = StringUtils.generateUUID();
    }

    public Date getCreated() {
        return created;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public List<String> getFlags() {

        if (flags == null) {
            return Lists.newArrayList();
        }

        return flags;
    }

    @JsonIgnore
    public String getFlagsSerialized() {
        if (flags != null) {
            return SerializationUtils.serializeToString(flags);
        } else {
            return flagsSerialized;
        }
    }

    public String getHash() {
        return hash;
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public List<UserConnectedService> getServices() {
        return services;
    }

    public String getUsername() {
        return username;
    }

    @JsonIgnore
    public String getLocale() {
        return getProfile().getLocale();
    }

    public boolean isBlocked() {
        return blocked;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    @JsonIgnore
    public boolean isDebug() {
        return debugUntil != null && debugUntil.after(new Date());
    }

    public Date getDebugUntil() {
        return debugUntil;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setFlags(List<String> flags) {
        if (flags == null) {
            return;
        }

        this.flags = flags;

        flagsSerialized = SerializationUtils.serializeToString(flags);
    }

    @JsonIgnore
    public void setFlagsSerialized(String flagsSerialized) {
        if (Strings.isNullOrEmpty(flagsSerialized)) {
            return;
        }

        this.flagsSerialized = flagsSerialized;

        if (!Strings.isNullOrEmpty(flagsSerialized)) {
            flags = SerializationUtils.deserializeFromString(flagsSerialized, STRING_LIST_TYPE_REFERENCE);
        }
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }

    public void setServices(List<UserConnectedService> services) {
        this.services = services;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDebugUntil(Date debugUntil) {
        this.debugUntil = debugUntil;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("username", username).toString();
    }

    /**
     * True/False if tracking is enabled for this user
     *
     * @return if tracking is enabled
     */
    @JsonIgnore
    public boolean isTrackingEnabled() {
        return flags == null || !FeatureFlags.FeatureFlagGroup.TRACKING_DISABLED.isFlagInGroup(flags);
    }
}
