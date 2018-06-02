package se.tink.backend.sms.gateways.cmtelecom.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkSmsResponseDetails {
    private String to;
    private String status;
    private String reference;
    private Integer parts;
    private String messageDetails;
    private Integer messageErrorCode;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Integer getMessageErrorCode() {
        return messageErrorCode;
    }

    public void setMessageErrorCode(Integer messageErrorCode) {
        this.messageErrorCode = messageErrorCode;
    }

    public String getMessageDetails() {
        return messageDetails;
    }

    public void setMessageDetails(String messageDetails) {
        this.messageDetails = messageDetails;
    }

    public Integer getParts() {
        return parts;
    }

    public String getReference() {
        return reference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
