package se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.consent.Scope.Weighted;

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
    protected static final Map<Integer, RedsysScope> WEIGHT_MAP = new HashMap<>();

    static {
        for (RedsysScope scope : RedsysScope.values()) {
            WEIGHT_MAP.put(scope.getWeight(), scope);
        }
    }

    private final String jsonName;
    private final int weight;

    @Override
    public RedsysScope extendIfNotAvailable(Set<RedsysScope> availableScopes) {
        RedsysScope outputScope = this;
        int outputWeight = this.getWeight();

        while (!availableScopes.contains(outputScope) && outputScope != null) {
            outputScope = WEIGHT_MAP.get(++outputWeight);
        }

        if (outputScope == null) {
            throw new IllegalStateException(
                    "[CONSENT GENERATOR] Extending scope failed. No wider scope available.");
        }

        return outputScope;
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
