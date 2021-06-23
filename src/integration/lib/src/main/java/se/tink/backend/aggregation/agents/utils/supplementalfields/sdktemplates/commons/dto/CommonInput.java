package se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.dto;

import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class CommonInput {
    @NotNull private final String description;
    private final String inputFieldHelpText;
    private final int inputFieldMaxLength;
    private final int inputFieldMinLength;
    private final String inputFieldPattern;
    private final String inputFieldPatternError;

    private final boolean sensitive;

    // Use it if input field must be in the group.
    private final InGroup inGroup;
}
