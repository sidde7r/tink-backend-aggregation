package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 * Defualt authentication types defined by BerlinGroup. Banks/ASPSPs can define their own types, so
 * deserializing to this enum can cause some issues.
 */
@RequiredArgsConstructor
public enum AuthenticationType {
    SMS_OTP("SMS_OTP"),
    CHIP_OTP("CHIP_OTP"),
    PHOTO_OTP("PHOTO_OTP"),
    PUSH_OTP("PUSH_OTP"),
    SMTP_OTP("SMTP_OTP");

    private final String type;

    public static Optional<AuthenticationType> fromString(String authType) {
        return Arrays.stream(AuthenticationType.values())
                .filter(x -> x.type.equalsIgnoreCase(authType))
                .findAny();
    }
}
