package se.tink.backend.aggregation.nxgen.http.persistent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class Header {
    private final String key;
    private final String value;

    @JsonCreator
    public Header(@JsonProperty("key") String key, @JsonProperty("value") String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Header && Objects.equals(this.hashCode(), obj.hashCode());
    }
}
