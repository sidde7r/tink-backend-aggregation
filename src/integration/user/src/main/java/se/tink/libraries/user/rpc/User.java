package se.tink.libraries.user.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import se.tink.libraries.enums.FeatureFlags;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private List<String> flags;
    private String flagsSerialized;
    private String id;

    @JsonInclude(Include.NON_NULL)
    private UserProfile profile;

    private String username;
    private Date debugUntil;

    public User() {
        id = UUIDUtils.generateUUID();
    }

    public List<String> getFlags() {

        if (flags == null) {
            return Lists.newArrayList();
        }

        return flags;
    }

    public String getId() {
        return id;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public String getUsername() {
        return username;
    }

    public Date getDebugUntil() {
        return debugUntil;
    }

    public void setFlags(List<String> flags) {
        if (flags == null) {
            return;
        }

        this.flags = flags;

        flagsSerialized = SerializationUtils.serializeToString(flags);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDebugUntil(Date debugUntil) {
        this.debugUntil = debugUntil;
    }

    @JsonIgnore
    public String getLocale() {
        return getProfile().getLocale();
    }

    @JsonIgnore
    public boolean isDebug() {
        return debugUntil != null && debugUntil.after(new Date());
    }

    @JsonIgnore
    public boolean isMultiCurrencyEnabled() {
        return FeatureFlags.FeatureFlagGroup.MULTI_CURRENCY_FOR_POCS.isFlagInGroup(getFlags());
    }
}
