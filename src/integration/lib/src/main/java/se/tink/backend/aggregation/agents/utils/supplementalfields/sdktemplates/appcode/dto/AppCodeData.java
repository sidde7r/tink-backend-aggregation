package se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.appcode.dto;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.dto.CommonInput;

@Builder
@Getter
public class AppCodeData {
    @NotNull private final String iconUrl;
    @NotNull private final String title;
    @NotNull private final List<String> instructions;

    @NotNull private final CommonInput input;
}
