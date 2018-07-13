package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BankIdOutput {
    @JsonProperty("StaticOutput")
    private StaticOutput staticOutput;

    public BankIdOutput() {

    }

    public BankIdOutput(BankIdModuleOutput moduleOutput) {
        this.staticOutput = new StaticOutput(moduleOutput);
    }


    public StaticOutput getStaticOutput() {
        return staticOutput;
    }

    public void setStaticOutput(StaticOutput staticOutput) {
        this.staticOutput = staticOutput;
    }

}
