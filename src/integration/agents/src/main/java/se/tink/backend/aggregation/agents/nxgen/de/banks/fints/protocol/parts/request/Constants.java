package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

public class Constants {

    public static final int UNINITIALIZED_SEGMENT_POSITION = 1;

    public static final String SECURITY_PROCEDURE = "PIN";
    public static final String ENCRYPTION_FUNCTION_CODE = "998"; // PlainText
    public static final String ENCRYPTION_SUPPLIER_ROLE =
            "1"; // Signer and publisher of message are the same person
    public static final String ENCRYPTION_BOUNDARY =
            "1"; // To what area is encryption applied, 1 means SHM, so for that message only?
    public static final String COMPRESSION_NONE = "0";

    public static final String PRODUCT_VERSION = "0.1";
    public static final String COUNTRY_CODE = "280";
    public static final String DEFAULT_BPD_VERSION = "0";
    public static final String DEFAULT_UPD_VERSION = "0";
    public static final String LANGUAGE_DE = "1";

    public static final String SYSTEM_ID_REQUIRED = "1";

    public static final String SYNC_MODE_NEW_CUSTOMER_ID = "0";
}
