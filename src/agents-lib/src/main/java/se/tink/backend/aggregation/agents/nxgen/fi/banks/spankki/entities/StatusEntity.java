package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.SpankkiConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StatusEntity {
    private Object errorTitle;
    private String errorMessage;
    private String lang;
    private Integer statusCode;
    private String statusCodeStr;
    private String errorInfo;
    private List<ErrorDetailsEntity> errorDetails;
    private List<Object> additionalInfo;

    @JsonIgnore
    public boolean isOK() {
        return SpankkiConstants.ServerResponse.OK == SpankkiConstants.ServerResponse.getMessage(this);
    }

    public Object getErrorTitle() {
        return errorTitle;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getLang() {
        return lang;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getStatusCodeStr() {
        return statusCodeStr;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public List<ErrorDetailsEntity> getErrorDetails() {
        return errorDetails;
    }

    public List<Object> getAdditionalInfo() {
        return additionalInfo;
    }
}
