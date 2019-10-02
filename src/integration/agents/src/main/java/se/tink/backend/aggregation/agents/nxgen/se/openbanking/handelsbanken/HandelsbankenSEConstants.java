package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken;

import se.tink.libraries.i18n.LocalizableKey;

public class HandelsbankenSEConstants {

    public static final int MAX_FETCH_PERIOD_MONTHS = 15;

    public static class BankIdUserMessage {
        public static final LocalizableKey ACTIVATION_NEEDED =
                new LocalizableKey("You need to activate your BankID in the Handelsbanken app.");
    }
}
