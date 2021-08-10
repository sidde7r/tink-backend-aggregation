package se.tink.backend.aggregation.agents.consent.generators.uk.starling;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import se.tink.backend.aggregation.agents.consent.ConsentGenerator;
import se.tink.backend.aggregation.agents.consent.ToScopes;
import se.tink.backend.aggregation.agents.consent.suppliers.ItemsSupplier;
import se.tink.backend.aggregation.agents.consent.suppliers.ScopesSupplier;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.libraries.credentials.service.RefreshableItem;

public class StarlingConsentGenerator implements ConsentGenerator<Set<String>> {

    private static final ToScopes<RefreshableItem, StarlingScope> itemToScopes =
            item -> {
                switch (item) {
                    case CHECKING_ACCOUNTS:
                    case SAVING_ACCOUNTS:
                    case CREDITCARD_ACCOUNTS:
                    case LOAN_ACCOUNTS:
                    case INVESTMENT_ACCOUNTS:
                        return Sets.newHashSet(
                                StarlingScope.ACCOUNT_READ,
                                StarlingScope.BALANCE_READ,
                                StarlingScope.ACCOUNT_HOLDER_TYPE_READ,
                                StarlingScope.ACCOUNT_HOLDER_NAME_READ,
                                StarlingScope.ACCOUNT_IDENTIFIER_READ);
                    case CHECKING_TRANSACTIONS:
                    case SAVING_TRANSACTIONS:
                    case CREDITCARD_TRANSACTIONS:
                    case LOAN_TRANSACTIONS:
                    case INVESTMENT_TRANSACTIONS:
                        return Sets.newHashSet(StarlingScope.TRANSACTION_READ);
                    case IDENTITY_DATA:
                    case LIST_BENEFICIARIES:
                    case TRANSFER_DESTINATIONS:
                    default:
                        return Collections.emptySet();
                }
            };

    private final ScopesSupplier<RefreshableItem, StarlingScope> scopesSupplier;

    private StarlingConsentGenerator(
            AgentComponentProvider componentProvider, Set<StarlingScope> availableScopes) {
        this.scopesSupplier =
                new ScopesSupplier<>(
                        ItemsSupplier.get(componentProvider.getCredentialsRequest()),
                        availableScopes,
                        itemToScopes);
    }

    public static StarlingConsentGenerator of(
            AgentComponentProvider componentProvider, Set<StarlingScope> availableScopes) {
        return new StarlingConsentGenerator(componentProvider, availableScopes);
    }

    @Override
    public Set<String> generate() {
        return scopesSupplier.getStrings();
    }
}
