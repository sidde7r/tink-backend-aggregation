package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MortgageListEntity {

    @JsonProperty("Mortgages")
    private List<MortgageEntity> mortgages;

    public List<MortgageEntity> getMortgages() {
        return mortgages;
    }

    public void setMortgages(List<MortgageEntity> mortgages) {
        this.mortgages = mortgages;
    }
}
