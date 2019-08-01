package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorModelEntity {
    @JsonProperty("timestamp")
    private OffsetDateTime timestamp = null;

    @JsonProperty("status")
    private Integer status = null;

    @JsonProperty("error")
    private String error = null;

    @JsonProperty("message")
    private String message = null;

    @JsonProperty("path")
    private String path = null;

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
