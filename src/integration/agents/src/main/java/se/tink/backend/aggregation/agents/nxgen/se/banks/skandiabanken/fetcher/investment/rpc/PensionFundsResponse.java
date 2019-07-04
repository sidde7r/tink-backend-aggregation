package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.pension.FundsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.pension.IdentificationEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.pension.PremiumsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.pension.SummaryEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PensionFundsResponse {

    @JsonProperty("Funds")
    private List<FundsEntity> funds;

    @JsonProperty("Identification")
    private IdentificationEntity identification;

    @JsonProperty("Premiums")
    private List<PremiumsEntity> premiums;

    @JsonProperty("Summary")
    private SummaryEntity summary;

    public List<FundsEntity> getFunds() {
        return funds;
    }

    public SummaryEntity getSummary() {
        return summary;
    }
}
