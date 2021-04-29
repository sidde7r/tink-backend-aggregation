package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OtpFormat {
    INTEGER("integer"),
    CHARACTERS("characters");

    private final String name;

    @JsonValue
    public String getName() {
        return name;
    }

    public static Optional<OtpFormat> fromString(String otpFormat) {
        return Arrays.stream(OtpFormat.values())
                .filter(x -> x.name.equalsIgnoreCase(otpFormat))
                .findAny();
    }
}
