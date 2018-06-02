package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShareDepotEntity {
    private String depotNumber;

    public String getDepotNumber() {
        return depotNumber;
    }

    public void setDepotNumber(String depotNumber) {
        this.depotNumber = depotNumber;
    }
}
