package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement
public class UserData {
    @JsonProperty("empresa")
    private String company;
    @JsonProperty("cliente")
    private CustomerData customer;
    @JsonProperty("canalMarco")
    private String channelFrame;
    @JsonProperty("contratoMulticanal")
    private ContractEntity multiChannelContract;

    public String getCompany() {
        return company;
    }

    public CustomerData getCustomer() {
        return customer;
    }

    public String getChannelFrame() {
        return channelFrame;
    }

    public ContractEntity getMultiChannelContract() {
        return multiChannelContract;
    }
}
