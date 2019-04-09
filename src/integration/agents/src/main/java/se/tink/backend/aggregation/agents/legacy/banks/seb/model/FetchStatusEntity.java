package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchStatusEntity {
    @JsonProperty("Status")
    private String status;

    @JsonProperty("SwrveStatus")
    private String swrveStaus;

    @JsonProperty("LoginMethods")
    private List<LoginMethodsEntity> loginMethods;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSwrveStaus() {
        return swrveStaus;
    }

    public void setSwrveStaus(String swrveStaus) {
        this.swrveStaus = swrveStaus;
    }

    public List<LoginMethodsEntity> getLoginMethods() {
        return loginMethods;
    }

    public void setLoginMethods(List<LoginMethodsEntity> loginMethods) {
        this.loginMethods = loginMethods;
    }
}
