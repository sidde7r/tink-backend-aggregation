package se.tink.backend.core;

import com.google.common.base.MoreObjects;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import se.tink.backend.core.enums.ApplicationFormStatusKey;

public class ApplicationFormStatus {
    @Tag(1)
    @ApiModelProperty(name = "key", value="A description of this Form.", example = "CREATED", allowableValues = ApplicationFormStatusKey.DOCUMENTED)
    private String key;
    @Tag(2)
    @ApiModelProperty(name = "message", value="A message describing the status", example = "null")
    private String message;
    @Tag(3)
    @ApiModelProperty(name = "updated", value="The updated timestamp.", example = "1469105426000")
    private Date updated;

    public ApplicationFormStatusKey getKey() {
        if (key == null) {
            return null;
        } else {
            return ApplicationFormStatusKey.valueOf(key);
        }
    }

    public void setKey(ApplicationFormStatusKey key) {
        if (key == null) {
            this.key = null;
        } else {
            this.key = key.toString();
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("key", key)
                .add("message", message)
                .add("updated", updated)
                .toString();
    }
}
