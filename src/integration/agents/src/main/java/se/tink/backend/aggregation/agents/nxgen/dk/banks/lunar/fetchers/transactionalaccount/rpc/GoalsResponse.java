package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc;

import java.util.List;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.GoalEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GoalsResponse {
    private List<GoalEntity> goals;

    public List<GoalEntity> getGoals() {
        return ListUtils.emptyIfNull(goals);
    }
}
