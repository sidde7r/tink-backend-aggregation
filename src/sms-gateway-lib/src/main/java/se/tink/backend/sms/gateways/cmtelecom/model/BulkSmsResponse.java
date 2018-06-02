package se.tink.backend.sms.gateways.cmtelecom.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkSmsResponse {
    private String details;
    private Integer errorCode;
    private List<BulkSmsResponseDetails> messages;

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public List<BulkSmsResponseDetails> getMessages() {
        return messages;
    }

    public void setMessages(List<BulkSmsResponseDetails> messages) {
        this.messages = messages;
    }

    /**
     * See documentation at https://docs.cmtelecom.com/bulk-sms/v1.0#/send_a_message
     */
    public boolean isSuccess() {
        return errorCode == 0 && (messages == null || messages.stream().allMatch(x -> x.getMessageErrorCode() == 0));
    }
}
