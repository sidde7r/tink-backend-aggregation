package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities;

import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class GetSignResponseEntity {

    private List<ChallengeEntity> challenges;
    private String summary;
    private String title;
}
