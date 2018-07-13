package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BankIdInput<MI> {
    @JsonProperty("ModuleInput")
    private MI moduleInput;

    public BankIdInput() {
        
    }
    
    public BankIdInput(MI moduleInput) {
        this.moduleInput = moduleInput;
    }

    public MI getModuleInput() {
        return moduleInput;
    }

    public void setModuleInput(MI moduleInput) {
        this.moduleInput = moduleInput;
    }
}
