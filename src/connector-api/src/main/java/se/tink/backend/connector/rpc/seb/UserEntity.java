package se.tink.backend.connector.rpc.seb;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;
import io.swagger.annotations.ApiModelProperty;
import java.util.Map;
import se.tink.backend.utils.LogUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEntity {

    private static final LogUtils log = new LogUtils(UserEntity.class);
    
    @ApiModelProperty(name = "externalId", value = "Persistent identifier for the user.", example = "2ce1f090a9304f13a15458d480f8a85d", required = true)
    private String externalId;
    @ApiModelProperty(name = "payload", value = "The payload property can include arbitrary metadata provided by the financial institution in question that can be used either for deep-linking back to the app of the financial institution, for displaying additional information about the user, etc. The format is key-value, where key is a String and value any object.", example = "{}", required = false)
    private Map<String, Object> payload;

    public String getExternalId() {
        return externalId;
    }
    
    public Map<String, Object> getPayload() {
        return payload;
    }
    
    /**
     * Check that all required fields are set and valid.
     */
    @JsonIgnore
    public boolean isValid() {
        if (Strings.isNullOrEmpty(externalId)) {
            log.info("'externalId' is null or empty for user");
            return false;
        }
        
        return true;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
    
    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
