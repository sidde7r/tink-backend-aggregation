package se.tink.backend.aggregation.agents.banks.nordea.v20.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.LightLoginResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {
    private LightLoginResponse lightLoginResponse;
    private BankingServiceResponse bankingServiceResponse;

    public LightLoginResponse getLightLoginResponse() {
        return lightLoginResponse;
    }

    public void setLightLoginResponse(LightLoginResponse lightLoginResponse) {
        this.lightLoginResponse = lightLoginResponse;
    }

    public BankingServiceResponse getBankingServiceResponse() {
        return bankingServiceResponse;
    }

    public void setBankingServiceResponse(BankingServiceResponse bankingServiceResponse) {
        this.bankingServiceResponse = bankingServiceResponse;
    }
}
