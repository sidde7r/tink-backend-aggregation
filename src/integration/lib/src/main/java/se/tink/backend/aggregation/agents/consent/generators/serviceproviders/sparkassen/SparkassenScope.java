package se.tink.backend.aggregation.agents.consent.generators.serviceproviders.sparkassen;

import java.util.EnumSet;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.consent.Scope.Weighted;
import se.tink.backend.aggregation.agents.consent.generators.WeightedExtender;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessType;

@RequiredArgsConstructor
@Getter
public enum SparkassenScope implements Weighted<SparkassenScope> {
    ACCOUNTS(0) {
        @Override
        public AccessEntity createAccessEntity() {
            return AccessEntity.builder().availableAccounts(AccessType.ALL_ACCOUNTS).build();
        }
    },
    ACCOUNTS_BALANCES_TRANSACTIONS(1) {
        @Override
        public AccessEntity createAccessEntity() {
            return AccessEntity.builder()
                    .availableAccountsWithBalance(AccessType.ALL_ACCOUNTS)
                    .build();
        }
    };

    private static final WeightedExtender<SparkassenScope> WEIGHTED_EXTENDER =
            new WeightedExtender(EnumSet.allOf(SparkassenScope.class));

    public abstract AccessEntity createAccessEntity();

    private final int weight;

    public SparkassenScope extendIfNotAvailable(Set<SparkassenScope> availableScopes) {
        return WEIGHTED_EXTENDER.extendIfNotAvailable(this, availableScopes);
    }
}
