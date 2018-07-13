package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BankIdModuleOutput {
    @JsonProperty("AutoStartToken")
    private String autoStartToken;
    @JsonProperty("BankIDStatusCode")
    private String bankIDStatusCode;
    @JsonProperty("BankIDStatusText")
    private String bankIDStatusText;
    @JsonProperty("OrderReference")
    private String orderReference;

    public String getAutoStartToken() {
        return autoStartToken;
    }

    public String getBankIDStatusCode() {
        return bankIDStatusCode;
    }

    public String getBankIDStatusText() {
        return bankIDStatusText;
    }

    public String getOrderReference() {
        return orderReference;
    }

    public void setAutoStartToken(String autoStartToken) {
        this.autoStartToken = autoStartToken;
    }

    public void setBankIDStatusCode(String bankIDStatusCode) {
        this.bankIDStatusCode = bankIDStatusCode;
    }

    public void setBankIDStatusText(String bankIDStatusText) {
        this.bankIDStatusText = bankIDStatusText;
    }

    public void setOrderReference(String orderReference) {
        this.orderReference = orderReference;
    }

}
