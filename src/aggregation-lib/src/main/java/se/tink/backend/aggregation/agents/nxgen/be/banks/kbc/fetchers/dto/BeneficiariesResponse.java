package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.HeaderResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BeneficiariesResponse extends HeaderResponse {
    private List<BeneficiaryDto> beneficiaries;

    public List<BeneficiaryDto> getBeneficiaries() {
        return beneficiaries;
    }
}
