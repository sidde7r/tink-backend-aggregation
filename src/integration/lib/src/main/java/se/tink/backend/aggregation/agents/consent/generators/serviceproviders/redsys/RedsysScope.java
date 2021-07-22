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
    AVAILABLE_ACCOUNTS_WITH_BALANCES("availableAccountsWithBalances", 2),
    ALL_PSD2("allPsd2", 3);

    protected static final int MIN_EXPIRATION_DAYS = 0;
    protected static final int MAX_EXPIRATION_DAYS = 90;
    protected static final int MIN_DAILY_FREQUENCY = 1;
    protected static final int MAX_DAILY_FREQUENCY = 4;
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

        while (!availableScopes.contains(outputScope) && outputWeight < 3) {
            outputScope = WEIGHT_MAP.get(++outputWeight);
        }

        if (!availableScopes.contains(outputScope)) {
            throw new RuntimeException(
                    "[CONSENT GENERATOR] Extending scope failed. Result scope not available: "
                            + outputScope);
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
