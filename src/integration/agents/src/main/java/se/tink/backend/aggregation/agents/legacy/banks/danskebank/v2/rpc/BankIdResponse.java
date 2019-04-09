package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

public class BankIdResponse {
    @JsonProperty("Output")
    private BankIdOutput output;

    public BankIdResponse() {}

    public BankIdResponse(BankIdModuleOutput moduleOutput) {
        output = new BankIdOutput(moduleOutput);
    }

    public BankIdOutput getOutput() {
        return output;
    }

    public void setOutput(BankIdOutput output) {
        this.output = output;
    }

    public String getOrderReference() {
        BankIdModuleOutput moduleOutput = getModuleOutput();
        if (moduleOutput == null) {
            return null;
        }

        return moduleOutput.getOrderReference();
    }

    public String getBankIdStatusCode() {
        BankIdModuleOutput moduleOutput = getModuleOutput();
        if (moduleOutput == null) {
            return null;
        }

        return moduleOutput.getBankIDStatusCode();
    }

    private BankIdModuleOutput getModuleOutput() {
        if (output == null
                || output.getStaticOutput() == null
                || output.getStaticOutput().getModuleOutput() == null) {
            return null;
        }

        return output.getStaticOutput().getModuleOutput();
    }

    public boolean isWaitingForUserInput() {
        return Objects.equal(getBankIdStatusCode(), "USER_SIGN")
                || Objects.equal(getBankIdStatusCode(), "OUTSTANDING_TRANSACTION");
    }

    public boolean isUserAuthenticated() {
        return Objects.equal(getBankIdStatusCode(), "COMPLETE");
    }

    public boolean isUserCancelled() {
        return Objects.equal(getBankIdStatusCode(), "USER_CANCEL");
    }

    public boolean isTimeout() {
        return Objects.equal(getBankIdStatusCode(), "NO_CLIENT")
                || Objects.equal(getBankIdStatusCode(), "EXPIRED_TRANSACTION")
                || Objects.equal(
                        getBankIdStatusCode(),
                        "CANCELLED"); // note difference between cancelled and user_cancel
    }
}
