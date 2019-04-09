package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BankIdModuleInput {
    private String device;

    @JsonProperty("devicetype")
    private String deviceType;

    private String operation;

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public BankIdModuleInput(String operation) {
        device = "other";
        deviceType = "mobile";
        this.operation = operation;
    }
}
