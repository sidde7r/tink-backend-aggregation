package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StaticOutput {
    @JsonProperty("ModuleOutput")
    private BankIdModuleOutput moduleOutput;
    @JsonProperty("FixedOutput")
    private FixedOutput fixedOutput;

    public StaticOutput() {

    }

    public StaticOutput(BankIdModuleOutput moduleOutput) {
        this.moduleOutput = moduleOutput;
    }

    public BankIdModuleOutput getModuleOutput() {
        return moduleOutput;
    }

    public void setModuleOutput(BankIdModuleOutput moduleOutput) {
        this.moduleOutput = moduleOutput;
    }

    public FixedOutput getFixedOutput() {
        return fixedOutput;
    }

    public void setFixedOutput(FixedOutput fixedOutput) {
        this.fixedOutput = fixedOutput;
    }

}
