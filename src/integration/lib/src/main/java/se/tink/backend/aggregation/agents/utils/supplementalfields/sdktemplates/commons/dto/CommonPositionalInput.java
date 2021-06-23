package se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.dto;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class CommonPositionalInput extends CommonInput {
    @NotNull private final List<Integer> positionOfFieldsToHide;

    public String getHint() {
        int hintLength = getInputFieldMaxLength();
        boolean positionGreaterThanHintLength =
                positionOfFieldsToHide.stream().anyMatch(position -> position >= hintLength);

        if (positionGreaterThanHintLength) {
            throw new IllegalStateException(
                    "Cannot generate hint - position greater than hint length");
        }

        return IntStream.range(0, hintLength)
                .mapToObj(i -> positionOfFieldsToHide.contains(i) ? "X" : "N")
                .collect(Collectors.joining(""));
    }
}
