package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.api.client.util.Strings;
import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AbstractResponse extends AbstractLinkResponse {
    protected String code;
    protected String desc;
    protected String message;
    private List<ResponseError> errors;
    private String detail;
    private String status;

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getMessage() {
        return message;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ResponseError> getErrors() {
        return errors;
    }

    public void setErrors(List<ResponseError> errors) {
        this.errors = errors;
    }

    @JsonIgnore
    private Optional<ResponseError> getFirstErrorWithErrorText() {
        if (getErrors() != null) {
            return getErrors().stream().filter(
                    input -> input != null && input.getDetail() != null && !input.getDetail().trim().isEmpty())
                    .findFirst();
        } else {
            return Optional.empty();
        }
    }

    @JsonIgnore
    public Optional<String> getFirstErrorMessage() {
        if (getFirstErrorWithErrorText().isPresent()) {
            return getFirstErrorWithErrorText().map(input -> input.getDetail().trim());
        } else if (Strings.isNullOrEmpty(detail)) {
            return Optional.ofNullable(detail);
        } else {
            return Optional.empty();
        }
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
