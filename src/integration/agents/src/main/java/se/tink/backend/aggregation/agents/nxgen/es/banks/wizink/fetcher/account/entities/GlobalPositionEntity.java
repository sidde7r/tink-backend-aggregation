package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.entities;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GlobalPositionEntity {
    private GlobalPositionDto globalPositionDto;

    public Optional<GlobalPositionDto> getGlobalPositionDto() {
        return Optional.ofNullable(globalPositionDto);
    }
}
