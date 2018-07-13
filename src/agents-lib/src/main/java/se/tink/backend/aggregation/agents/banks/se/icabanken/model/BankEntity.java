package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BankEntity {
    @JsonProperty("Name")
    private String name;
    @JsonProperty("TransferBankId")
    private String transferBankId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTransferBankId() {
        return transferBankId;
    }

    public void setTransferBankId(String transferBankId) {
        this.transferBankId = transferBankId;
    }
}
