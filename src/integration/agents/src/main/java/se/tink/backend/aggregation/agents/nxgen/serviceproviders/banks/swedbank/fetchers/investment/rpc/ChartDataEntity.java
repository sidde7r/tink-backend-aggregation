package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChartDataEntity {
    private List<AllocationEntity> allocations;
    private String chartsDataMissingText;

    public List<AllocationEntity> getAllocations() {
        return allocations;
    }

    public String getChartsDataMissingText() {
        return chartsDataMissingText;
    }
}
