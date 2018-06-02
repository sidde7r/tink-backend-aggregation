package se.tink.backend.consent.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.text.ParseException;
import java.util.Date;
import java.util.Objects;
import se.tink.backend.consent.core.cassandra.CassandraConsent;
import se.tink.libraries.versioning.SemanticVersion;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class UserConsent {
    @ApiModelProperty(name = "id", value = "The id of the consent.", example = "87d194a449e8443da07cfefc5081574d")
    @Tag(1)
    private String id;

    @ApiModelProperty(name = "key", value = "The key of the consent.", example = "APP_TERMS_AND_CONDITIONS")
    @Tag(2)
    private String key;

    @ApiModelProperty(name = "version", value = "The version of the consent.", example = "1.0.0")
    @Tag(3)
    private String version;

    @ApiModelProperty(name = "action", value = "The action executed by the user.", example = "ACCEPTED", allowableValues = Action.DOCUMENTED)
    @Tag(4)
    private Action action;

    @ApiModelProperty(name = "timestamp", value = "The timestamp wen the consent was given or withdrawn (accepted or declined).", example = "442912200000")
    @Tag(5)
    private Date timestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isCompatibleWith(CassandraConsent consent) {
        return isCompatibleWith(consent.getKey(), consent.getVersion());
    }

    /**
     * Returns true of this user consent is a compatible with the input using semantic versioning. We consider a user
     * consent to be compatible with another one if the major version is equal or larger to the other consent.
     * Examples:
     * - User gave consent for 1.0.0. Input is 1.0.1 => Compatible
     * - User gave consent for 1.1.0. Input is 2.0.0 => Not compatible
     */
    boolean isCompatibleWith(String inputKey, String inputVersion) {
        if (!Objects.equals(this.key, inputKey)) {
            return false;
        }

        SemanticVersion version1;
        SemanticVersion version2;

        try {
            version1 = new SemanticVersion(this.getVersion());
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format("Could not parse = '%s')", this.getVersion()), e);
        }

        try {
            version2 = new SemanticVersion(inputVersion);
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format("Could not parse = '%s')", inputVersion), e);
        }

        return version1.major >= version2.major;
    }
}
