package se.tink.backend.aggregation.agents.utils.authentication.encap2.rpc;

import java.util.Objects;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class AbstractResponse<T> {
    private int code;
    private T result;
    private boolean outdated;

    public int getCode() {
        return code;
    }

    public T getResult() {
        return result;
    }

    public boolean isOutdated() {
        return outdated;
    }

    public boolean isValid() {
        return !outdated && code == 0 && Objects.nonNull(result);
    }
}
