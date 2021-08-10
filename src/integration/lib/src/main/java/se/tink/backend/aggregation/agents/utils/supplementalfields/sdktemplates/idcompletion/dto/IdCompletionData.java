package se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.idcompletion.dto;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.dto.CommonInput;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.dto.CommonPositionalInput;

@Builder
@Getter
public class IdCompletionData {
    @NotNull private final String colorHex;

    @NotNull private final String title;

    @NotNull private final String identityHintImage; // can be both base64 / url
    @NotNull private final String identityHintText;

    @NotNull private final CommonInput passwordInput;

    @NotNull private final List<CommonPositionalInput> identifications;
}
