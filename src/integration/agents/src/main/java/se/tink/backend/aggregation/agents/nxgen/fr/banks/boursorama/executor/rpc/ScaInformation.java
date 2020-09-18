package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.rpc;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScaInformation {
    private List<ChallengeDetailsEntity> challenges;
}
