package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.FinancialInstitutionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FinancialInstituationsListResponse {
    private List<FinancialInstitutionEntity> financialInstitutions;

    public List<FinancialInstitutionEntity> getFinancialInstitutions() {
        return financialInstitutions;
    }
}
