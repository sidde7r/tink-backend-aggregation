package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.protostuff.Exclude;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.Type;
import se.tink.backend.agents.core.UserConnectedService;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.enums.FeatureFlags;
import se.tink.libraries.strings.StringUtils;

@Entity
@Table(name = "users")
@SuppressWarnings("serial")
public class User implements Serializable {
    private static final TypeReference<List<String>> STRING_LIST_TYPE_REFERENCE = new TypeReference<List<String>>() {
    };

    @JsonIgnore
    @Exclude
    @ApiModelProperty(name = "blocked", hidden = true)
    private boolean blocked;
    @JsonIgnore
    @Exclude
    @ApiModelProperty(name = "confirmed", hidden = true)
    private boolean confirmed;
    @Tag(10)
    private Date created;
    @Tag(1)
    @ApiModelProperty(name = "endpoint", hidden = true)
    private String endpoint;
    @Tag(8)
    private List<String> flags;
    @Tag(2)
    private String flagsSerialized;
    @JsonIgnore
    @Exclude
    @ApiModelProperty(name = "hash", hidden = true)
    private String hash;
    @Tag(3)
    private String id;
    @Creatable
    @Modifiable
    @JsonInclude(Include.NON_NULL)
    @Tag(4)
    private String password;
    @Creatable
    @Tag(5)
    private UserProfile profile;
    @JsonInclude(Include.NON_NULL)
    @Tag(6)
    @ApiModelProperty(name = "services", hidden = true)
    private List<UserConnectedService> services;
    @Creatable
    @Modifiable
    @Tag(7)
    private String username;
    @Exclude
    @ApiModelProperty(name = "debugUntil", hidden = true)
    private Date debugUntil;
    @Tag(9)
    private String nationalId;

    public User() {
        id = StringUtils.generateUUID();
    }

    @ApiModelProperty(name = "created", value = "The date when the user was created.", required = true)
    public Date getCreated() {
        return created;
    }

    public String getEndpoint() {
        return endpoint;
    }

    @Transient
    @ApiModelProperty(name = "flags", value = "The user-specific feature flags assigned to the user.", example = "[\"TRANSFERS\", \"TEST_PINK_ONBOARDING\"]")
    public List<String> getFlags() {

        if (flags == null) {
            return Lists.newArrayList();
        }

        return flags;
    }

    @JsonIgnore
    @Column(name = "`flags`")
    @Type(type = "text")
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

    @Id
    @ApiModelProperty(name = "id", value = "The internal identifier of the user.", example = "6e68cc6287704273984567b3300c5822", required = true)
    public String getId() {
        return id;
    }

    @Transient
    @ApiModelProperty(name = "password", value = "The password of the user (only included at registration).")
    public String getPassword() {
        return password;
    }

    @Embedded
    @ApiModelProperty(name = "profile", value = "The configurable profile of the user", required = true)
    @Nonnull
    public UserProfile getProfile() {
        return profile;
    }

    @Transient
    public List<UserConnectedService> getServices() {
        return services;
    }

    @ApiModelProperty(name = "username", value = "The username (usually email) of the user.", example = "nisse@manpower.se")
    public String getUsername() {
        return username;
    }

    @JsonIgnore
    @Transient
    @Nonnull
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
    @Transient
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
    @Transient
    public boolean isTrackingEnabled() {
        return flags == null || !FeatureFlags.FeatureFlagGroup.TRACKING_DISABLED.isFlagInGroup(flags);
    }
}
