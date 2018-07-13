package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.StringEscapeUtils;
import se.tink.backend.aggregation.agents.BankIdStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthBankIdPollResponse {

    @JsonProperty("status")
    private String status;

    @JsonProperty("text")
    private String text;

    @JsonProperty("padding")
    private String padding;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = StringEscapeUtils.unescapeHtml(text);
    }

    public String getPadding() {
        return padding;
    }

    public void setPadding(String padding) {
        this.padding = padding;
    }

    /**
     * The status is "done" for both successful and failed signing processes, so the text value must be checked.
     */
    public BankIdStatus getBankIdStatus() {
        if (text.toLowerCase().contains("åtgärden avbruten")) {
            return BankIdStatus.CANCELLED;
        } else if (text.toLowerCase().contains("inget svar")) {
            return BankIdStatus.TIMEOUT;
        } else if (text.toLowerCase().contains("var god vänta")) {
            return BankIdStatus.DONE;
        } else {
            return BankIdStatus.WAITING;
        }
    }
}
