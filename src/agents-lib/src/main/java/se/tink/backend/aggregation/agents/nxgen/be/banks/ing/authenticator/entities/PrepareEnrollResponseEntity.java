package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities;

import com.google.common.base.Preconditions;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.json.BaseMobileResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PrepareEnrollResponseEntity extends BaseMobileResponseEntity {
    private List<String> challenges;
    private String signingId;

    public List<String> getChallenges() {
        return Preconditions.checkNotNull(challenges);
    }

    public String getSigningId() {
        return Preconditions.checkNotNull(signingId);
    }
}
