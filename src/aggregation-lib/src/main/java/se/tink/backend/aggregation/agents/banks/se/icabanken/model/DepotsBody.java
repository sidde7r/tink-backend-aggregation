package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DepotsBody {
    @JsonProperty("Depots")
    private List<DepotEntity> depots;

    public List<DepotEntity> getDepots() {
        return depots;
    }

    public void setDepots(List<DepotEntity> depots) {
        this.depots = depots;
    }
}
