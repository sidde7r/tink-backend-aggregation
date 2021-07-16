package se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.cardreader.dto;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.dto.CommonInput;

@Getter
@Builder
public class CardReaderData {
    @NotNull private final String secondFactorDescription;
    @NotNull private final String secondFactorValue;

    @NotNull private final CommonInput input;

    private final String instructionFieldDescription;
    private final List<String> instructions;
}
