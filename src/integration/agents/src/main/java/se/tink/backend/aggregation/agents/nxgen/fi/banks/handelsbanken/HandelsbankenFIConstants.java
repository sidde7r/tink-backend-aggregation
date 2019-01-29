package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken;

import java.util.Arrays;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.http.URL;

public class HandelsbankenFIConstants {

    public static final class Urls {
        public static final URL ENTRY_POINT = new URL("https://m.handelsbanken.se/open/entrypoint/fipriv");
    }

    public static final class Authentication {
        public static final LogTag LOG_TAG = LogTag.from("#fi_handelsbanken_login_refactoring");
    }

    public static final class DeviceAuthentication {
        public static final String APP_ID = "glsOOxjLhKc8lXoJNKsksa7RCbYTMGYjigyk1KfrPY8=";
        public static final String AUTH_TP = "3";
        public static final String SIGNUP_PASSWORD = "signupPassword";
        public static final String VALID_SIGNATURE_RESULT = "AUTHORIZED";
    }

    public static final class Headers {
        public static final String APP_VERSION = "3.4:1.5";
    }

    public static final class Fetcher {

        public static final class Loans {
            public static final LogTag LOG_TAG = LogTag.from("#fi_handelsbanken_loans");
        }
    }

    public enum LoanType {
        MISC_LOAN("muu laina", LoanDetails.Type.OTHER),
        STUDENT_LOAN("opintolaina", LoanDetails.Type.STUDENT),
        HOUSING_LOAN("asuntolaina", LoanDetails.Type.MORTGAGE),
        OTHER_LOAN("", LoanDetails.Type.OTHER);

        private final String name;
        private final LoanDetails.Type tinkType;

        LoanType(String name, LoanDetails.Type type) {
            this.name = name;
            this.tinkType = type;
        }

        public LoanDetails.Type getTinkType() {
            return this.tinkType;
        }

        public static LoanType findLoanType(String name) {
            return Arrays.stream(LoanType.values())
                    .filter(loanType -> loanType.name.equalsIgnoreCase(name)).findFirst().orElse(OTHER_LOAN);
        }
    }
}
