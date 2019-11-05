package se.tink.backend.aggregation.agents.utils.authentication.encap3.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class AbstractResponse<T> {
    @JsonProperty private int code;
    @JsonProperty private T result;
    @JsonProperty private boolean outdated;

    @JsonIgnore
    public int getCode() {
        return code;
    }

    @JsonIgnore
    public T getResult() {
        return result;
    }

    @JsonIgnore
    public boolean isOutdated() {
        return outdated;
    }

    @JsonIgnore
    public boolean isValid() {
        return !outdated && code == 0 && Objects.nonNull(result);
    }
}
