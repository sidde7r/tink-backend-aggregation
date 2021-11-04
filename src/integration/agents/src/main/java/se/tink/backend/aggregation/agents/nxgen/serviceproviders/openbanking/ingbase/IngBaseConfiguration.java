package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase;

import com.google.common.collect.Sets;
import java.util.Set;
import se.tink.backend.aggregation.agents.consent.generators.nl.ing.IngScope;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class IngBaseConfiguration implements ClientConfiguration {

    public static Set<IngScope> getIngScopes() {
        return Sets.newHashSet(IngScope.VIEW_PAYMENT_BALANCES, IngScope.VIEW_PAYMENT_TRANSACTIONS);
    }
}
