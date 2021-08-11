package se.tink.backend.aggregation.agents.consent.generators.serviceproviders.nordea;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import se.tink.backend.aggregation.agents.consent.ConsentGenerator;
import se.tink.backend.aggregation.agents.consent.ToScopes;
import se.tink.backend.aggregation.agents.consent.suppliers.ItemsSupplier;
import se.tink.backend.aggregation.agents.consent.suppliers.ScopesSupplier;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.libraries.credentials.service.RefreshableItem;

public class NordeaConsentGenerator implements ConsentGenerator<Set<String>> {

    private static final ToScopes<RefreshableItem, NordeaScope> itemToScopes =
            item -> {
                switch (item) {
                    case SAVING_ACCOUNTS:
                    case LOAN_ACCOUNTS:
                    case INVESTMENT_ACCOUNTS:
                    case CHECKING_ACCOUNTS:
                    case IDENTITY_DATA:
                        return Sets.newHashSet(
                                NordeaScope.READ_ACCOUNT_BASIC,
                                NordeaScope.READ_ACCOUNT_BALANCES,
                                NordeaScope.READ_ACCOUNTS_DETAIL);
                    case CREDITCARD_ACCOUNTS:
                        return Sets.newHashSet(
                                NordeaScope.READ_ACCOUNT_BASIC,
                                NordeaScope.READ_ACCOUNT_BALANCES,
                                NordeaScope.READ_ACCOUNTS_DETAIL,
                                NordeaScope.READ_CARDS_INFORMATION);
                    case SAVING_TRANSACTIONS:
                    case LOAN_TRANSACTIONS:
                    case INVESTMENT_TRANSACTIONS:
                    case CHECKING_TRANSACTIONS:
                        return Sets.newHashSet(NordeaScope.READ_ACCOUNTS_TRANSACTIONS);
                    case CREDITCARD_TRANSACTIONS:
                        return Sets.newHashSet(
                                NordeaScope.READ_ACCOUNTS_TRANSACTIONS,
                                NordeaScope.READ_CARDS_TRANSACTIONS);
                    case LIST_BENEFICIARIES:
                    case TRANSFER_DESTINATIONS:
                    default:
                        return Collections.emptySet();
                }
            };

    private final ScopesSupplier<RefreshableItem, NordeaScope> scopesSupplier;

    public NordeaConsentGenerator(
            AgentComponentProvider componentProvider, Set<NordeaScope> availableScopes) {
        this.scopesSupplier =
                new ScopesSupplier<>(
                        ItemsSupplier.get(componentProvider.getCredentialsRequest()),
                        availableScopes,
                        itemToScopes);
    }

    public static NordeaConsentGenerator of(
            AgentComponentProvider componentProvider, Set<NordeaScope> availableScopes) {
        return new NordeaConsentGenerator(componentProvider, availableScopes);
    }

    @Override
    public Set<String> generate() {
        return scopesSupplier.getStrings();
    }
}
