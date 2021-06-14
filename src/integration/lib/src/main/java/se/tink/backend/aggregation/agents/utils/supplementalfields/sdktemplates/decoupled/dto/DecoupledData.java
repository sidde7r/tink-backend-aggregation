package se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.decoupled.dto;

import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class DecoupledData {
    @NotNull private final String iconUrl;
    @NotNull private final String instruction;
}
