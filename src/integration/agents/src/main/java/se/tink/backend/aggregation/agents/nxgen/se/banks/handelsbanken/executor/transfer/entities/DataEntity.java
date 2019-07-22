package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DataEntity {

    @JsonProperty("transfer-data")
    private String transferData;

    public DataEntity setTransferData(String transferData) {
        this.transferData = transferData;
        return this;
    }
}
