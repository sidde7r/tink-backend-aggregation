package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UkOpenBankingV31PaymentConstants {

    static final ImmutableList<String> PREFERRED_ID_TOKEN_SIGNING_ALGORITHM =
            ImmutableList.<String>builder()
                    .add(SIGNING_ALGORITHM.PS256.toString())
                    .add(SIGNING_ALGORITHM.RS256.toString())
                    .build();
    static final String TINK_UK_OPEN_BANKING_ORG_ID = "00158000016i44IAAQ";
    static final String UKOB_TAN = "openbanking.org.uk";
    static final String MONZO_ORG_ID = "001580000103U9RAAU";
    static final String DANSKEBANK_ORG_ID = "0015800000jf7AeAAI";
    static final String HSBC_ORG_ID = "00158000016i44JAAQ";
    static final String NATIONWIDE_ORG_ID = "0015800000jf8aKAAQ";
    static final String RBS_ORG_ID = "0015800000jfwB4AAI";
    static final String ULSTER_ORG_ID = "0015800000jfxrpAAA";
    static final String NATWEST_ORG_ID = "0015800000jfwxXAAQ";
    static final String BARCLAYS_ORG_ID = "0015800000jfAW1AAM";
    static final String RFC_2253_DN =
            "CN=00158000016i44IAAQ, OID.2.5.4.97=PSDSE-FINA-44059, O=Tink AB, C=GB";
    static final String GENERAL_STANDARD_ISS = "1f1YEdOMw6AphlVC6k2JQR";

    public enum SIGNING_ALGORITHM {
        RS256,
        PS256
    }

    public static class Storage {

        public static final String CONSENT_ID = "consentId";
        public static final String PAYMENT_ID = "paymentId";

        private Storage() {}
    }

    public static class Step {

        public static final String AUTHORIZE = "AUTHORIZE";
        static final String SUFFICIENT_FUNDS = "SUFFICIENT_FUNDS";
        static final String EXECUTE_PAYMENT = "EXECUTE_PAYMENT";

        private Step() {}
    }

    public static class PaymentStatusCode {

        public static final String AWAITING_AUTHORISATION = "AwaitingAuthorisation";

        private PaymentStatusCode() {}
    }

    public static class FormValues {

        public static final String PAYMENT_CREDITOR_DEFAULT_NAME = "Payment Receiver";

        private FormValues() {}
    }

    public static class Errors {
        public static final String ACCESS_DENIED = "access_denied";
        static final String LOGIN_REQUIRED = "login_required";
    }
}
