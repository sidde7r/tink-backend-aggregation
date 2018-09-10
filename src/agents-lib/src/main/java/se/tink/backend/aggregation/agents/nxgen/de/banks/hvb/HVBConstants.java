package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb;

import com.google.common.collect.ImmutableSet;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.URL;

// NOTE: Look out for additional, Worklight-specific constants in the WLConstants class

public final class HVBConstants {
    private HVBConstants() {
        throw new AssertionError();
    }

    public static class Url {
        public static final URL ENDPOINT = new URL("https://my.hypovereinsbank.de/" + HVBConstants.MODULE_NAME);
    }

    public enum Storage {
        ACCOUNT_NUMBER
    }

    public static final String MODULE_NAME = "HVBApp";
    public static final String APP_ID = "de.unicredit.apptan";

    public static final String CERT_TYPE = "X.509";

    // Found hardcoded in the iOS app. Used for generating the 'a' and 'b' parameters found in outgoing /query messages.
    public static final String SYMMETRIC_CERTIFICATE =
            "MIIFAjCCA+qgAwIBAgIEMadFvjANBgkqhkiG9w0BAQUFADCBnTELMAkGA1UEBhMC"
                    + "REUxDzANBgNVBAgTBkJheWVybjERMA8GA1UEBwwITcO8bmNoZW4xGjAYBgNVBAoT"
                    + "EVVuaUNyZWRpdCBCYW5rIEFHMRAwDgYDVQQLEwdYTDc4NDU4MRQwEgYDVQQDEwtj"
                    + "ZXJ0V2ViVmlldzEmMCQGCSqGSIb3DQEJARYXaW5mb0BoeXBvdmVyZWluc2Jhbmsu"
                    + "ZGUwHhcNMTYwOTI4MDAwMDAwWhcNMzYwOTI3MjM1OTU5WjCBnTELMAkGA1UEBhMC"
                    + "REUxDzANBgNVBAgTBkJheWVybjERMA8GA1UEBwwITcO8bmNoZW4xGjAYBgNVBAoT"
                    + "EVVuaUNyZWRpdCBCYW5rIEFHMRAwDgYDVQQLEwdYTDc4NDU4MRQwEgYDVQQDEwtj"
                    + "ZXJ0V2ViVmlldzEmMCQGCSqGSIb3DQEJARYXaW5mb0BoeXBvdmVyZWluc2Jhbmsu"
                    + "ZGUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC/NWTh9glrlqkz9PZ7"
                    + "0sr0s6A6HE4rLSNGcqptdADWeYGH+jzfkxDpknXrrio7XlYRtQG370DU9EY0DjOY"
                    + "UVAaGAzetu6hzkbSyRowODYEgJmnW+FGt23QrCV7LQUrMgyYR1Mdu4dD2qsRglag"
                    + "fwQUVuQJa9pbv4RIgLPji5PcWdvPZwHG21v694T296WhdzQzubUjyTxEowvYVu5o"
                    + "9A7/XNe7mmI7o/Qap9wUxd5dAJpHLuvjiAc3RQO2O+8y1pGUYXvF1zTz+AuyHAcv"
                    + "GiK4eh132dN32X0/8UCqcKDHwVSyu5kVttGU+ZFG76O53ypnhpxT5z0CQm5R+rAo"
                    + "fmofAgMBAAGjggFGMIIBQjAMBgNVHRMBAf8EAjAAMB0GA1UdDgQWBBTXKDsNYdNe"
                    + "xQXWu8jyIQsHpCwYdTCBzQYDVR0jBIHFMIHCgBTXKDsNYdNexQXWu8jyIQsHpCwY"
                    + "daGBo6SBoDCBnTELMAkGA1UEBhMCREUxDzANBgNVBAgTBkJheWVybjERMA8GA1UE"
                    + "BwwITcO8bmNoZW4xGjAYBgNVBAoTEVVuaUNyZWRpdCBCYW5rIEFHMRAwDgYDVQQL"
                    + "EwdYTDc4NDU4MRQwEgYDVQQDEwtjZXJ0V2ViVmlldzEmMCQGCSqGSIb3DQEJARYX"
                    + "aW5mb0BoeXBvdmVyZWluc2JhbmsuZGWCBDGnRb4wCwYDVR0PBAQDAgTwMBEGCWCG"
                    + "SAGG+EIBAQQEAwIGQDAjBglghkgBhvhCAQ0EFhYUY2VyZl9mb3JfV0VCVklFV19I"
                    + "VkIwDQYJKoZIhvcNAQEFBQADggEBAC3OCYLfO0p3WMfJ4uPgI8w3f8GN9yT6oasQ"
                    + "6IztAwLNMKMRpjhoNVjEaiXOC3qgG0KcZN1jhPDvlLL8yJB7KINA4M00RWw59zL7"
                    + "34dC8ppzbWpFMZ+3i/lG231yzFQExKcBFEjS4ENLjqKFj0B8TdkXy2oEJq1oRTJm"
                    + "JkzTX/dRzFICZpvtgIh2EwG0UfkdBi1Ibj94yw7Wr++pbgLg/I4cL/NGtDGlbHZk"
                    + "VY04ZL62s9Qa9HK5LlMXCCtXaI8Oixq1B+RkyR5rov5IBy5NhEgOEqvJvN7JlNR0"
                    + "NgKyrHSPgFEsGXJdsPkHCkdDLNaZgFebgI2YHD3bljTgJODwrv8=";

    public static final ImmutableSet<String> CHECKING_ACCOUNT_TITLE_SUBSTRINGS = ImmutableSet.<String>builder()
            .add("KONTO START") // Observed as "HVB Konto Start", a.k.a. "HVB StartKonto"
            .add("AKTIVKONTO") // Observed as "HVB AktivKonto"
            .add("PLUSKONTO")
            .add("EXKLUSIVKONTO")
            .build();

    public static final ImmutableSet<String> SAVINGS_ACCOUNT_TITLE_SUBSTRINGS = ImmutableSet.<String>builder()
            .add("KOMFORTSPAREN")
            .add("SPARKARTE") // Observed as "FCB SparKarte"
            .build();

    public enum LogTags {
        HVB_UNKNOWN_ACCOUNT_TYPE,
        HVB_UNRECOGNIZED_ACCOUNT_TYPE;

        public LogTag toTag() {
            return LogTag.from("#" + name());
        }
    }
}
