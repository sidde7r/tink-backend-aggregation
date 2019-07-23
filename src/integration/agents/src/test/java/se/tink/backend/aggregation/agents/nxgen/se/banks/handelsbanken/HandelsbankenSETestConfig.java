package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class HandelsbankenSETestConfig {

    public static final String S_NO = "";
    public static final String PASSWORD = "";

    public static final Number AMOUNT = 1.1284d;

    public static final String SOURCE_ACCOUNT = "";

    public static final String UNKNOWN_DESTINATION_ACCOUNT = "";
    public static final String KNOWN_DESTINATION_ACCOUNT = "";

    public static final String PAYMENT_DESTINATION_ACCOUNT = "";
    public static final String PAYMENT_MESSAGE = "";

    public static final String E_INVOICE_DESTINATION_ACCOUNT = "";
    public static final String E_INVOICE_MESSAGE = "";
    public static final String E_APPROVAL_ID =
            "MDAwNDQ1NjcxODgwMDAzMTFEMjZBMDF8MjAxOS0wNy0xMC0yMC41OC4wMy42MDcyNTA";

    public static final String ERROR_MESSAGE = "Translated error text";
    public static final PersistentStorage PERSISTENT_STORAGE = new PersistentStorage();

    static {
        PERSISTENT_STORAGE.put(HandelsbankenConstants.Storage.PRIVATE_KEY, "");
        PERSISTENT_STORAGE.put(HandelsbankenConstants.Storage.PROFILE_ID, "");
        PERSISTENT_STORAGE.put(HandelsbankenConstants.Storage.DEVICE_SECURITY_CONTEXT_ID, "");
    }
}
