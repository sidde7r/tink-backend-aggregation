package se.tink.backend.aggregation.agents.banks.nordea.v15.model.bankid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.BankingServiceResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MobileBankIdInitialAuthenticationResponse {
    @JsonProperty("initBankIdAuthenticationOut")
    private MobileBankIdInitialAuthenticationResponseData data;

    @JsonProperty("bankingServiceResponse")
    private BankingServiceResponse serviceResponse;

    public MobileBankIdInitialAuthenticationResponseData getData() {
        return data;
    }

    public BankingServiceResponse getServiceResponse() {
        return serviceResponse;
    }

    public void setData(MobileBankIdInitialAuthenticationResponseData data) {
        this.data = data;
    }

    public void setServiceResponse(BankingServiceResponse serviceResponse) {
        this.serviceResponse = serviceResponse;
    }
}
