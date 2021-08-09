package se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys;

import java.util.EnumSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.consent.Scope.Weighted;
import se.tink.backend.aggregation.agents.consent.generators.WeightedExtender;

@RequiredArgsConstructor
public enum RedsysScope implements Weighted<RedsysScope> {
    // Detailed consent model
    ACCOUNTS("accounts", 0),
    BALANCES("balances", 0),
    TRANSACTIONS("transactions", 0),

    // Global consent model
    AVAILABLE_ACCOUNTS("availableAccounts", 1),
    AVAILABLE_ACCOUNTS_WITH_BALANCES("availableAccountsWithBalances", 1),
    ALL_PSD2("allPsd2", 2);

    public static final int MIN_EXPIRATION_DAYS = 0;
    public static final int MAX_EXPIRATION_DAYS = 90;
    public static final int MIN_DAILY_FREQUENCY = 1;
    public static final int MAX_DAILY_FREQUENCY = 4;
    private static final WeightedExtender<RedsysScope> WEIGHTED_EXTENDER =
            new WeightedExtender(EnumSet.allOf(RedsysScope.class));

    private final String jsonName;
    private final int weight;

    @Override
    public RedsysScope extendIfNotAvailable(Set<RedsysScope> availableScopes) {
        return WEIGHTED_EXTENDER.extendIfNotAvailable(this, availableScopes);
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return jsonName;
    }
}
