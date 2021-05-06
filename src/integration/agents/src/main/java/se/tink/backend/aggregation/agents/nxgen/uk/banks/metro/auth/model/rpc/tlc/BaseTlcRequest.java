package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.BaseTlcRequest.HeaderEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@EqualsAndHashCode
public class BaseTlcRequest<T, V extends HeaderEntity> {

    private final List<V> headers;

    private final T data;

    public BaseTlcRequest(V header, T data) {
        this.headers = Collections.singletonList(header);
        this.data = data;
    }

    public List<V> getHeaders() {
        return headers;
    }

    public T getData() {
        return data;
    }

    @JsonObject
    @EqualsAndHashCode
    public abstract static class HeaderEntity {

        @JsonProperty("type")
        private String type;

        public HeaderEntity(String type) {
            this.type = type;
        }

        @JsonIgnore
        public abstract String getValue();

        public HeaderEntity setType(String type) {
            this.type = type;
            return this;
        }

        public String getType() {
            return type;
        }
    }
}
