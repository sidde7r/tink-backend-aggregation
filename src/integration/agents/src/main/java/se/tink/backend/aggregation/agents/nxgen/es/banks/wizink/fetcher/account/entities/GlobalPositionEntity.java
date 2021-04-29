package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class GlobalPositionEntity {
    private GlobalPositionDto globalPositionDto;
}
