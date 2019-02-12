package se.tink.backend.aggregation.agents.abnamro.client.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPreferences {
    private String id;
    @JsonProperty("value")
    private Map<String, String> values;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getValues() {
        return values;
    }

    public void setValue(Map<String, String> value) {
        this.values = value;
    }

    public Optional<Boolean> getBoolean(String key) {
        
        if (values == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(BooleanUtils.toBoolean(values.get(Preconditions.checkNotNull(key))));
    }
}
