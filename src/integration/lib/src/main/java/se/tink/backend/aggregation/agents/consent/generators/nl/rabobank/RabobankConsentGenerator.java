package se.tink.backend.aggregation.agents.consent.generators.nl.rabobank;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import se.tink.backend.aggregation.agents.consent.ConsentGenerator;
import se.tink.backend.aggregation.agents.consent.ToScopes;
import se.tink.backend.aggregation.agents.consent.suppliers.ItemsSupplier;
import se.tink.backend.aggregation.agents.consent.suppliers.ScopesSupplier;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.libraries.credentials.service.RefreshableItem;

public class RabobankConsentGenerator implements ConsentGenerator<String> {

    private static final ToScopes<RefreshableItem, RabobankScope> itemToScopes =
            item -> {
                switch (item) {
                    case CHECKING_ACCOUNTS:
                        return Sets.newHashSet(RabobankScope.READ_BALANCES);
                    case CHECKING_TRANSACTIONS:
                        return Sets.newHashSet(
                                RabobankScope.READ_TRANSACTIONS_90DAYS,
                                RabobankScope.READ_TRANSACTIONS_HISTORY);
                    case SAVING_ACCOUNTS:
                    case CREDITCARD_ACCOUNTS:
                    case LOAN_ACCOUNTS:
                    case INVESTMENT_ACCOUNTS:
                    case SAVING_TRANSACTIONS:
                    case CREDITCARD_TRANSACTIONS:
                    case LOAN_TRANSACTIONS:
                    case INVESTMENT_TRANSACTIONS:
                    case IDENTITY_DATA:
                    case LIST_BENEFICIARIES:
                    case TRANSFER_DESTINATIONS:
                    default:
                        return Collections.emptySet();
                }
            };

    private final ScopesSupplier<RefreshableItem, RabobankScope> scopesSupplier;

    public RabobankConsentGenerator(
            AgentComponentProvider componentProvider, Set<RabobankScope> availableScopes) {
        this.scopesSupplier =
                new ScopesSupplier<>(
                        ItemsSupplier.get(componentProvider.getCredentialsRequest()),
                        availableScopes,
                        itemToScopes);
    }

    public static RabobankConsentGenerator of(
            AgentComponentProvider componentProvider, Set<RabobankScope> availableScopes) {
        return new RabobankConsentGenerator(componentProvider, availableScopes);
    }

    @Override
    public String generate() {
        return String.join(" ", scopesSupplier.getStrings());
    }
}
