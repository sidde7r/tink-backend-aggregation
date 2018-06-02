package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorizeBankIdResponse extends AbstractResponse {
    private String result;
    private List<MandateEntity> mandates;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<MandateEntity> getMandates() {
        return mandates;
    }

    public void setMandates(List<MandateEntity> mandates) {
        this.mandates = mandates;
    }
}
