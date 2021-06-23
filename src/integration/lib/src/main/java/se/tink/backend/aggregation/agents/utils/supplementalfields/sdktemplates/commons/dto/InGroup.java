package se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class InGroup {
    private final String group;
    private final boolean oneOf;
}
