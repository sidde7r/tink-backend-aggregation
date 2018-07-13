package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {
    @XmlElement
    protected String authToken;

    @XmlAttribute
    protected String code;

    @XmlAttribute
    protected String label;

    public String getAuthToken() {
        return authToken;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
