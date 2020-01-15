package se.tink.backend.aggregation.agents.nxgen.it.banks.ing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IngAgentConstants {

    public static final String DATE_OF_BIRTH = "date-of-birth";
    public static final String DATE_OF_BIRTH_FORMAT = "ddMMyyyy";
    public static final int DEVICE_ID_LENGTH = 32;

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Storage {
        public static final String JSESSION_ID = "jSessionId";
        public static final String DEVICE_ID = "DeviceId";
    }
}
