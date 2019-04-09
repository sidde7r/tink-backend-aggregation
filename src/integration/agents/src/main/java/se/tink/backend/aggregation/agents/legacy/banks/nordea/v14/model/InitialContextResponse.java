package se.tink.backend.aggregation.agents.banks.nordea.v14.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.xml.bind.annotation.XmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement
public class InitialContextResponse {
    protected BankingServiceResponse bankingServiceResponse;
    protected InitialContext getInitialContextOut;

    public InitialContext getGetInitialContextOut() {
        return getInitialContextOut;
    }

    public BankingServiceResponse getBankingServiceResponse() {
        return bankingServiceResponse;
    }

    public void setBankingServiceResponse(BankingServiceResponse bankingServiceResponse) {
        this.bankingServiceResponse = bankingServiceResponse;
    }

    public void setGetInitialContextOut(InitialContext getInitialContextOut) {
        this.getInitialContextOut = getInitialContextOut;
    }
}
