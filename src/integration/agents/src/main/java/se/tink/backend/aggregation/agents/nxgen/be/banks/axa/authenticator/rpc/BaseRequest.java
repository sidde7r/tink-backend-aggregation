package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.HeaderEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public abstract class BaseRequest<T> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private T data;
    private List<HeaderEntity> headers;

    public BaseRequest(T data, List<HeaderEntity> headers) {
        this.data = data;
        this.headers = headers;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<HeaderEntity> getHeaders() {
        return headers;
    }

    public void setHeaders(List<HeaderEntity> headers) {
        this.headers = headers;
    }

    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Couldn't parse to json", e);
        }
    }
}
