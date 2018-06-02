package se.tink.backend.consent.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.google.common.base.Strings;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import se.tink.backend.consent.core.Action;
import se.tink.backend.consent.core.exceptions.ConsentRequestInvalid;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class ConsentRequest {
    @ApiModelProperty(name = "key", value = "The key of the consent.", example = "APP_TERMS_AND_CONDITIONS")
    @Tag(1)
    private String key;

    @ApiModelProperty(name = "version", value = "The version of the consent.", example = "1.0.0")
    @Tag(2)
    private String version;

    @ApiModelProperty(name = "action", value = "The action executed by the user.", example = "ACCEPTED", allowableValues = Action.DOCUMENTED)
    @Tag(3)
    private Action action;

    @ApiModelProperty(name = "checksum", value = "Checksum of the consent. Used to verify that the server version hasn't been changes", required = true, example = "7cb7d8321fa63ed7b7b5db8c7389c201e7970a647c862d975e407e09617a1156")
    @Tag(4)
    private String checksum;

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

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public void validate() throws ConsentRequestInvalid {
        if (Strings.isNullOrEmpty(key)) {
            throw new ConsentRequestInvalid("Key is null or empty.");
        }

        if (Strings.isNullOrEmpty(version)) {
            throw new ConsentRequestInvalid("Version is null or empty.");
        }

        if (Strings.isNullOrEmpty(checksum)) {
            throw new ConsentRequestInvalid("Checksum is null or empty.");
        }

        if (action == null) {
            throw new ConsentRequestInvalid("Action is null.");
        }
    }
}
