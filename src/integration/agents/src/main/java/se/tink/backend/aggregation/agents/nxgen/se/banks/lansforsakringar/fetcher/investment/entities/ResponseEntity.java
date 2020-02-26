package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResponseEntity {
    // `hasSavingEngagement` is null - cannot define it!
    // `hasProtectionEngagement` is null - cannot define it!
    private List<EngagementsEntity> engagements;
    private boolean livSystemOpen;

    public List<EngagementsEntity> getEngagements() {
        return engagements;
    }

    public boolean isLivSystemOpen() {
        return livSystemOpen;
    }
}
