package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.investment.entities;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OccupationalPensionInsurancesEntity {
    private List<PensionInsuranceEntity> pensionInsurances;
    private AmountEntity totalValue;

    public List<PensionInsuranceEntity> getPensionInsurances() {
        return pensionInsurances == null ? Collections.emptyList() : pensionInsurances;
    }
}
