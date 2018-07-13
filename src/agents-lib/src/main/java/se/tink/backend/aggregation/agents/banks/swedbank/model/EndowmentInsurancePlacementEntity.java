package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EndowmentInsurancePlacementEntity {
    private String name;
    private List<EndowmentInsuranceSubPlacementEntity> subPlacements;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<EndowmentInsuranceSubPlacementEntity> getSubPlacements() {
        return subPlacements;
    }

    public void setSubPlacements(
            List<EndowmentInsuranceSubPlacementEntity> subPlacements) {
        this.subPlacements = subPlacements;
    }
}
