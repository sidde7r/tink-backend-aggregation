package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.errors;

import se.tink.backend.aggregation.agents.utils.berlingroup.error.TppMessage;

public class SparkassenKnownErrors {

    static class PsuErrorMessages {
        public static final String REQUEST_PROCESSING_ERROR =
                "Die Anfrage konnte nicht verarbeitet werden.";
        static final String TEMPORARILY_BLOCKED_ACCOUNT =
                "Ihr Zugang ist vorläufig gesperrt - Bitte PIN-Sperre aufheben";
        static final String BLOCKED_ACCOUNT =
                "Ihr Zugang ist gesperrt - Bitte informieren Sie Ihren Berater";
        static final String NO_ACTIVE_TAN_MEDIUM = "Kein aktives TAN-Medium gefunden.";
        static final String PLEASE_CHANGE_PIN = "Bitte führen Sie eine PIN-Änderung durch.";
        static final String CUSTOMER_NOT_FOUND =
                "9070- Der Auftrag wurde nicht ausgeführt. - 9931- Anmeldename oder PIN ist falsch.";
    }

    private static final String FORMAT_ERROR = "FORMAT_ERROR";

    static final TppMessage PSU_CREDENTIALS_INVALID =
            TppMessage.builder().category(TppMessage.ERROR).code("PSU_CREDENTIALS_INVALID").build();

    static final TppMessage OTP_WRONG_FORMAT =
            TppMessage.builder()
                    .category(TppMessage.ERROR)
                    .code(FORMAT_ERROR)
                    .text(
                            "Format of certain request fields are not matching the XS2A requirements.")
                    .build();
    static final TppMessage OTP_WRONG_LENGTH =
            TppMessage.builder()
                    .category(TppMessage.ERROR)
                    .code(FORMAT_ERROR)
                    .text("scaAuthenticationData muss auf Ausdruck \"[0-9]{6}\" passen")
                    .build();

    static final TppMessage PSU_TOO_LONG =
            TppMessage.builder()
                    .category(TppMessage.ERROR)
                    .code(FORMAT_ERROR)
                    .text("PSU-ID zu lang.")
                    .build();

    static final TppMessage NO_SCA_METHOD =
            TppMessage.builder()
                    .category(TppMessage.ERROR)
                    .code("SCA_INVALID")
                    .text("No active/ usable scaMethods defined for PSU.")
                    .build();

    static final TppMessage CONSENT_INVALID =
            TppMessage.builder()
                    .category(TppMessage.ERROR)
                    .code("CONSENT_INVALID")
                    .text("PSU not authorized for account access.")
                    .build();

    static final TppMessage CONSENT_UNKNOWN =
            TppMessage.builder().category(TppMessage.ERROR).code("CONSENT_UNKNOWN").build();

    static final TppMessage REQUEST_PROCESSING_ERROR =
            TppMessage.builder()
                    .category(TppMessage.ERROR)
                    .text(PsuErrorMessages.REQUEST_PROCESSING_ERROR)
                    .build();

    public static final TppMessage PAYMENT_STATUS_UNKNOWN =
            TppMessage.builder().category(TppMessage.ERROR).code("PAYMENT_STATUS_UNKNOWN").build();
}
