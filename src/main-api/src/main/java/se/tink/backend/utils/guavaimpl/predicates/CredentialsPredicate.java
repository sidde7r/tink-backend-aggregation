package se.tink.backend.utils.guavaimpl.predicates;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import se.tink.backend.core.Credentials;

public class CredentialsPredicate {
    private static final ImmutableSet<String> BANKS_WITHOUT_SAVINGS_ACCOUNT_INTEREST_RATE = ImmutableSet.<String>builder()
            .add("handelsbanken")
            .add("seb")
            .add("swedbank")
            .add("savingsbank")
            .add("nordea")
            .add("danskebank")
            .add("skandiabanken")
            .add("lansforsakringar")
            .add("sparbankensyd")
            .build();

    public static Predicate<Credentials> IS_BANK_WITHOUT_SAVINGS_ACCOUNT_INTEREST_RATE = c -> {
        String providerName = c.getProviderName();
        if (Strings.isNullOrEmpty(providerName)) {
            return false;
        }

        if (providerName.contains("-")) {
            providerName = providerName.substring(0, providerName.indexOf('-'));
        }

        return BANKS_WITHOUT_SAVINGS_ACCOUNT_INTEREST_RATE.contains(providerName);
    };

    public static final Predicate<Credentials> CREDENTIAL_IS_CREDIT_SAFE = credentials -> Objects
            .equal(credentials.getProviderName(), "creditsafe");
}
