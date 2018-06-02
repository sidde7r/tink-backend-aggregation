package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IskPlacementEntity {
    private String name;
    private List<IskSubPlacementEntity> subPlacements;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<IskSubPlacementEntity> getSubPlacements() {
        return subPlacements;
    }

    public void setSubPlacements(
            List<IskSubPlacementEntity> subPlacements) {
        this.subPlacements = subPlacements;
    }
}
