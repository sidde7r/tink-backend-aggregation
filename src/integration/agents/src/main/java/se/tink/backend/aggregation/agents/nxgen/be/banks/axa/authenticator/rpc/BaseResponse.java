package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import java.util.function.Function;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.HeaderEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public abstract class BaseResponse<T> {

    private T data;
    private Integer errorCode;
    private String errorMessage;
    private List<HeaderEntity> headers;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<HeaderEntity> getHeaders() {
        return headers;
    }

    public void setHeaders(List<HeaderEntity> headers) {
        this.headers = headers;
    }

    public String getUid() {
        return getHeaderValue("ephemeral_uid", HeaderEntity::getUid);
    }

    public String getDeviceId() {
        return getHeaderValue("device_id", HeaderEntity::getDeviceId);
    }

    public String getSessionId() {
        return getHeaderValue("session_id", HeaderEntity::getSessionId);
    }

    private String getHeaderValue(String headerType, Function<HeaderEntity, String> valueMapper) {
        return headers.stream()
                .filter(headerEntity -> headerType.equals(headerEntity.getType()))
                .findAny()
                .map(valueMapper)
                .orElseThrow(IllegalStateException::new);
    }
}
