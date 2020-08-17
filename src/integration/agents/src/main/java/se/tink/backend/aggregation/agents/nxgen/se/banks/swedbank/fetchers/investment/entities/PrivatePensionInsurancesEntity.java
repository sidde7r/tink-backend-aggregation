package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.entities;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PrivatePensionInsurancesEntity {
    private List<PensionInsuranceEntity> pensionInsurances;

    public List<PensionInsuranceEntity> getPensionInsurances() {
        return pensionInsurances == null ? Collections.emptyList() : pensionInsurances;
    }
}
