package se.tink.backend.aggregation.agents.consent.generators.nl.ics;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import se.tink.backend.aggregation.agents.consent.ConsentGenerator;
import se.tink.backend.aggregation.agents.consent.ToScopes;
import se.tink.backend.aggregation.agents.consent.suppliers.ItemsSupplier;
import se.tink.backend.aggregation.agents.consent.suppliers.ScopesSupplier;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.libraries.credentials.service.RefreshableItem;

public class IcsConsentGenerator implements ConsentGenerator<Set<String>> {

    private static final ToScopes<RefreshableItem, IcsScope> itemToScopes =
            item -> {
                switch (item) {
                    case CREDITCARD_ACCOUNTS:
                        return Sets.newHashSet(
                                IcsScope.READ_ACCOUNT_BASIC,
                                IcsScope.READ_ACCOUNTS_DETAIL,
                                IcsScope.READ_BALANCES);
                    case CREDITCARD_TRANSACTIONS:
                        return Sets.newHashSet(
                                IcsScope.READ_TRANSACTION_BASIC,
                                IcsScope.READ_TRANSACTIONS_DETAIL,
                                IcsScope.READ_TRANSACTIONS_CREDITS,
                                IcsScope.READ_TRANSACTIONS_DEBITS);
                    case CHECKING_ACCOUNTS:
                    case CHECKING_TRANSACTIONS:
                    case SAVING_ACCOUNTS:
                    case LOAN_ACCOUNTS:
                    case INVESTMENT_ACCOUNTS:
                    case SAVING_TRANSACTIONS:
                    case LOAN_TRANSACTIONS:
                    case INVESTMENT_TRANSACTIONS:
                    case IDENTITY_DATA:
                    case LIST_BENEFICIARIES:
                    case TRANSFER_DESTINATIONS:
                    default:
                        return Collections.emptySet();
                }
            };

    private final ScopesSupplier<RefreshableItem, IcsScope> scopesSupplier;

    public IcsConsentGenerator(
            AgentComponentProvider componentProvider, Set<IcsScope> availableScopes) {
        this.scopesSupplier =
                new ScopesSupplier<>(
                        ItemsSupplier.get(componentProvider.getCredentialsRequest()),
                        availableScopes,
                        itemToScopes);
    }

    public static IcsConsentGenerator of(
            AgentComponentProvider componentProvider, Set<IcsScope> availableScopes) {
        return new IcsConsentGenerator(componentProvider, availableScopes);
    }

    @Override
    public Set<String> generate() {
        return scopesSupplier.getStrings();
    }
}
