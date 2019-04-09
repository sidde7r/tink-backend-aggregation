package se.tink.libraries.enums;

import com.google.common.collect.ImmutableSet;
import java.util.List;

/**
 * Class for feature flags. Reason why enum is not used is because the flexibility to add feature
 * flags in DB directly without having to add code.
 */
public class FeatureFlags {

    // Flags

    public static final String TEST_BADGES_AND_LOOKBACK = "TEST_BADGES_AND_LOOKBACK_ON";
    public static final String TEST_MANUAL_REFRESH_REMINDER = "TEST_MANUAL_REFRESH_REMINDER_ON";
    public static final String STATUS_MESSAGES_OFF = "STATUS_MESSAGES_OFF";
    public static final String TEST_FRAUD_ANDROID = "TEST_ANDROID_FRAUD_ON";
    public static final String TEST_FRAUD_ONLY = "TEST_ANDROID_FRAUD_ONLY_ON";
    public static final String FRAUD_PROTECTION = "FRAUD_PROTECTION";
    public static final String ANDROID_BETA = "ANDROID_BETA";
    public static final String IOS_BETA = "IOS_BETA";
    public static final String MERCHANTIFICATION_CLUSTER = "MERCHANTIFICATION_CLUSTER_ON";
    public static final String DEMO_USER_ON = "DEMO_USER_ON";
    public static final String DEMO_USER_OFF = "DEMO_USER_OFF";
    public static final String TINK_EMPLOYEE = "TINK_EMPLOYEE";
    public static final String TINK_TEST_ACCOUNT = "TINK_TEST_ACCOUNT";
    public static final String TEST_FRAUD_REMINDERS_ON = "TEST_FRAUD_REMINDERS_ON";
    public static final String ABN_AMRO_PILOT_CUSTOMER = "ABN_AMRO_PILOT_CUSTOMER";
    public static final String ABN_AMRO_DETAILED_PUSH_NOTIFICATIONS =
            "ABN_AMRO_DETAILED_PUSH_NOTIFICATIONS";
    public static final String ABN_AMRO_ICS_NEW_ACCOUNT_FORMAT = "ABN_AMRO_ICS_NEW_ACCOUNT_FORMAT";
    public static final String TRANSFERS = "TRANSFERS";
    public static final String ANONYMOUS = "ANONYMOUS";
    public static final String TEST_DISCOVER_ON = "TEST_DISCOVER_ON";
    public static final String KEEP_ALIVE = "KEEP_ALIVE";
    public static final String APPLICATIONS = "APPLICATIONS";
    public static final String EXPERIMENTAL = "EXPERIMENTAL";
    public static final String NO_TINK_USER = "NO_TINK_USER";
    public static final String NO_EXTRAPOLATION = "TEST_NO_EXTRAPOLATION_ON";
    public static final String DETECT_NATIONAL_ID = "DETECT_NATIONAL_ID";
    public static final String TRANSACTION_PROCESSOR_V2 = "TRANSACTION_PROCESSOR_V2";
    public static final String RESIDENCE_VALUATION = "RESIDENCE_VALUATION";
    public static final String ONBOARDING_V1 = "ONBOARDING_V1";
    public static final String ONBOARDING_V2 = "ONBOARDING_V2";
    public static final String ONBOARDING_MORTGAGE = "ONBOARDING_MORTGAGE";
    public static final String SPLIT_TRANSACTIONS = "SPLIT_TRANSACTIONS";
    // jrenold: temporary feature flag to enable handelsbanken's new api endpoint
    public static final String TEMP_SHB_NEW_API = "TEMP_SHB_NEW_API";
    public static final String FASTTEXT_CATEGORIZER = "FASTTEXT_CATEGORIZER";
    // Flags for moving users to new providers
    public static final String COOP_V2 = "COOP_V2";
    public static final String TMP_TEST_OAUTH_GRPC = "TMP_TEST_OAUTH_GRPC";
    public static final String ALANDSBANKEN_SE_V4 = "ALANDSBANKEN_SE_V4";
    public static final String PRODUCTS_OPT_OUT = "PRODUCTS_OPT_OUT";

    // Flags for testing Actionable Insights
    public static final String TEST_ACTIONABLE_INSIGHTS = "TEST_ACTIONABLE_INSIGHTS";

    // Dynamic flag if the residence tab should be shown in the apps
    public static final String RESIDENCE_TAB = "RESIDENCE_TAB";
    // This flag enables Multi Currency phase 1 for a user. This means that this user will recieve
    // accounts and
    // transactions in multiple currencies, but statistics calculations will not be done for foreign
    // currencies.
    // The currency is currently stored in a rather hackish way, this is why we don't want to enable
    // it
    // across the board. // jelgh 2018-11-15
    public static final String MULTI_CURRENCY_PHASE_1 = "MULTI_CURRENCY_PHASE_1";

    // Flag for rolling the ICS duplication fix
    public static final String ABN_AMRO_ICS_DUPLICATE_FIX = "ABN_AMRO_ICS_DUPLICATE_FIX";

    /**
     * A feature groups hold information what flags are included and what client version are
     * required.
     */
    public enum FeatureFlagGroup {
        FRAUD_ONLY_FEATURE(ImmutableSet.of(TEST_FRAUD_ONLY), "2.0.0", "1.9.3"),
        FRAUD_FEATURE(ImmutableSet.of(TEST_FRAUD_ANDROID, FRAUD_PROTECTION)),
        TRACKING_DISABLED(ImmutableSet.of(TINK_TEST_ACCOUNT, NO_TINK_USER)),
        FRAUD_OFF_LOCATION(ImmutableSet.of(TINK_EMPLOYEE, ANDROID_BETA)),
        FRAUD_FEATURE_V2(ImmutableSet.of(FRAUD_PROTECTION), "2.0.1", "2.0.3"),
        TRANSFERS_FEATURE(ImmutableSet.of(TRANSFERS, TINK_EMPLOYEE, IOS_BETA, ANDROID_BETA)),
        APPLICATIONS_FEATURE(
                ImmutableSet.of(TINK_EMPLOYEE, APPLICATIONS, IOS_BETA),
                "2.5.5",
                "999.9.9"), // Disabled for Android.
        RESIDENCE_VALUATION_FEATURE(
                ImmutableSet.of(TINK_EMPLOYEE, IOS_BETA, RESIDENCE_VALUATION),
                "2.5.14",
                "999.9.9"), // Disabled for Android.
        SPLIT_TRANSACTIONS_FEATURE(
                ImmutableSet.of(SPLIT_TRANSACTIONS),
                "999.9.9",
                "999.9.9"), // TODO: Set the client versions to align with their compatibility with
        // split transactions.
        MULTI_CURRENCY_FOR_POCS(ImmutableSet.of(TINK_EMPLOYEE, MULTI_CURRENCY_PHASE_1));

        private ImmutableSet<String> flags;
        private String minIosVersion;
        private String minAndroidVersion;

        FeatureFlagGroup(ImmutableSet<String> flags) {
            this.flags = flags;
        }

        FeatureFlagGroup(
                ImmutableSet<String> flags, String minIosVersion, String minAndroidVersion) {
            this.flags = flags;
            this.minIosVersion = minIosVersion;
            this.minAndroidVersion = minAndroidVersion;
        }

        public boolean isFlagInGroup(List<String> userFlags) {
            if (userFlags == null) {
                return false;
            }

            for (String flag : userFlags) {
                if (this.flags.contains(flag)) {
                    return true;
                }
            }

            return false;
        }
    }
}
