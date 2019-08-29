package se.tink.libraries.credentials.demo;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;

public enum DemoCredentials {
    USER1("190101019990"),
    USER2("191212121212"),
    USER3("200101019990"),
    USER4("201212121212"),
    USER5("201212121213", DemoUserFeature.RANDOMIZE_TRANSACTIONS),
    USER6("194111021111"),
    USER7("181001018835"),
    USER8("201212121214", DemoUserFeature.REQUIRES_SUPPLEMENTAL_INFORMATION),
    USER9("201212121215", DemoUserFeature.REQUIRES_BANK_ID),
    USER10("201212121216"),
    USER11("201212121217"),
    USER12("201212121218"), // 2 accounts, 10 transactions
    USER13(
            "201212121219",
            DemoUserFeature.GENERATE_TRANSACTIONS), // 1 account, transactions that will generate
    // fraud/identity events
    GROWTH_USER("demoUser"),
    USER14("201212121220"), // Credentials with good test data for split transactions
    USER15("201212121221"), // Credentials with good test data for unsecured loans
    ;

    public enum DemoUserFeature {
        RANDOMIZE_TRANSACTIONS,
        REQUIRES_SUPPLEMENTAL_INFORMATION,
        REQUIRES_BANK_ID,
        GENERATE_TRANSACTIONS
    }

    private final String username;

    private ImmutableSet<DemoUserFeature> features;

    private static final Map<String, DemoCredentials> demoUserByUsername = Maps.newHashMap();

    static {
        for (DemoCredentials user : DemoCredentials.values()) {
            String key = user.getUsername();
            if (demoUserByUsername.containsKey(key)) {
                throw new RuntimeException("Demo users with the same username.");
            }
            demoUserByUsername.put(key, user);
        }
    }

    /**
     * Get a demo user by username.
     *
     * @param username a username to be checked.
     * @return a demo user, or null if not a demo user username.
     */
    public static DemoCredentials byUsername(String username) {
        return demoUserByUsername.get(username);
    }

    public static boolean isDemoUser(String username) {
        return username != null && demoUserByUsername.containsKey(username);
    }

    DemoCredentials(String username, DemoUserFeature... features) {
        this.username = username;
        this.features = ImmutableSet.copyOf(features);
    }

    public String getUsername() {
        return username;
    }

    public boolean hasFeature(DemoUserFeature feature) {
        return features.contains(feature);
    }
}
