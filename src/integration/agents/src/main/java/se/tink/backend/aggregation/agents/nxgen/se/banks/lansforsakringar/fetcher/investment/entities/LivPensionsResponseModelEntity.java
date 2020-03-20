package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LivPensionsResponseModelEntity {
    private List<PrivatePensionsEntity> privatePensions;
    private List<OccupationalPensionsEntity> occupationalPensions;
    private List<CapitalInsurancesEntity> capitalInsurances;
    // `error` is null - cannot define it!

    @JsonIgnore
    public boolean isPrivatPensionsEmpty() {
        return privatePensions.isEmpty();
    }

    @JsonIgnore
    public boolean isCapitalInsurancesEmpty() {
        return capitalInsurances.isEmpty();
    }

    public List<OccupationalPensionsEntity> getOccupationalPensions() {
        return occupationalPensions;
    }

    public List<CapitalInsurancesEntity> getCapitalInsurances() {
        return capitalInsurances;
    }
}
