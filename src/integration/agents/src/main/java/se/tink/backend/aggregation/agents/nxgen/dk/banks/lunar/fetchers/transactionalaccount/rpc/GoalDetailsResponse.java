package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc;

import java.util.List;
import lombok.Data;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.FeedEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class GoalDetailsResponse {
    private List<FeedEntity> feed;
    private String goalId;

    public List<FeedEntity> getFeed() {
        return ListUtils.emptyIfNull(feed);
    }
}
