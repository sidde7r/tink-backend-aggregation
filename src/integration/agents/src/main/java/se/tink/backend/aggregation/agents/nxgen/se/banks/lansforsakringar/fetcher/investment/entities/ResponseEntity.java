package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import com.google.api.client.util.Lists;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResponseEntity {
    // `hasSavingEngagement` is null - cannot define it!
    // `hasProtectionEngagement` is null - cannot define it!
    private List<EngagementsEntity> engagements;
    private boolean livSystemOpen;

    public List<EngagementsEntity> getEngagements() {
        return Optional.ofNullable(engagements).orElse(Lists.newArrayList());
    }

    public boolean isLivSystemOpen() {
        return livSystemOpen;
    }
}
