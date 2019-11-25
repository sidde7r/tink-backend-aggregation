package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.loan;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.generic.SectionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetLoanAccountsBody {

    @JsonProperty("Seccao")
    private SectionEntity section;

    public SectionEntity getSection() {
        return section;
    }
}
