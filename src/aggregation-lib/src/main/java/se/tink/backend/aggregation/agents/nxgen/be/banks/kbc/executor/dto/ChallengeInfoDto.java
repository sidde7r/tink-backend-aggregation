package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.executor.dto;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChallengeInfoDto {

    private List<ParamFieldDto> paramFields;
}
