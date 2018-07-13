package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityEntity {
    private String securityText;
    private String securityType;

    public String getSecurityText() {
        return securityText;
    }

    public void setSecurityText(String securityText) {
        this.securityText = securityText;
    }

    public String getSecurityType() {
        return securityType;
    }

    public void setSecurityType(String securityType) {
        this.securityType = securityType;
    }
}
