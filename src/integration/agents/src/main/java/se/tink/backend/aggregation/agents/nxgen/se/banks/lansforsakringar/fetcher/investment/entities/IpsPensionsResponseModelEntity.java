package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IpsPensionsResponseModelEntity {
    private List<IpsPensionsEntity> ipsPensions;
    // `error` is null - cannot define it!

    @JsonIgnore
    public boolean isEmpty() {
        return ipsPensions.isEmpty();
    }
}
