package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnore;
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
