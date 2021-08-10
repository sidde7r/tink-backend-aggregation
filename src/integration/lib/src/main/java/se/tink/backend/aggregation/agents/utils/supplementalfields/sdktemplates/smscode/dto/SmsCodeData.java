package se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.smscode.dto;

import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.dto.CommonInput;

@Builder
@Getter
public class SmsCodeData {
    @NotNull private final String iconUrl;

    @NotNull private final String title;

    @NotNull private final CommonInput input;
}
