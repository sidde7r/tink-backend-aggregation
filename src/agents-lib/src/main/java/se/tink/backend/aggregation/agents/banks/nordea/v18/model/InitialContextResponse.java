package se.tink.backend.aggregation.agents.banks.nordea.v18.model;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement
public class InitialContextResponse {
    private BankingServiceResponse bankingServiceResponse;
    @JsonProperty("getInitialContextOut")
    private InitialContextData data;

    public InitialContextData getData() {
        return data;
    }

    public BankingServiceResponse getBankingServiceResponse() {
        return bankingServiceResponse;
    }

    public void setBankingServiceResponse(BankingServiceResponse bankingServiceResponse) {
        this.bankingServiceResponse = bankingServiceResponse;
    }

    public void setData(InitialContextData data) {
        this.data = data;
    }
}
