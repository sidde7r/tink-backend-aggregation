package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.FinancialInstitutionEntity;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinancialInstituationsListResponse {
    private List<FinancialInstitutionEntity> financialInstitutions;

    public List<FinancialInstitutionEntity> getFinancialInstitutions() {
        return financialInstitutions;
    }
}
