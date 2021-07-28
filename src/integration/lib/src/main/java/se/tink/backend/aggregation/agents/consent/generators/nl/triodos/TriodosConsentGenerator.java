package se.tink.backend.aggregation.agents.consent.generators.nl.triodos;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import se.tink.backend.aggregation.agents.consent.ConsentGenerator;
import se.tink.backend.aggregation.agents.consent.ToScopes;
import se.tink.backend.aggregation.agents.consent.suppliers.ItemsSupplier;
import se.tink.backend.aggregation.agents.consent.suppliers.ScopesSupplier;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AccessEntity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.libraries.credentials.service.RefreshableItem;

public class TriodosConsentGenerator implements ConsentGenerator<AccessEntity> {

    private static final ToScopes<RefreshableItem, TriodosScope> itemToScopes =
            item -> {
                switch (item) {
                    case CHECKING_ACCOUNTS:
                    case SAVING_ACCOUNTS:
                        return Sets.newHashSet(TriodosScope.ACCOUNTS, TriodosScope.BALANCES);
                    case CHECKING_TRANSACTIONS:
                    case SAVING_TRANSACTIONS:
                        return Sets.newHashSet(TriodosScope.TRANSACTIONS);
                    case CREDITCARD_ACCOUNTS:
                    case LOAN_ACCOUNTS:
                    case INVESTMENT_ACCOUNTS:
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

    private final ScopesSupplier<RefreshableItem, TriodosScope> scopesSupplier;

    public TriodosConsentGenerator(
            AgentComponentProvider componentProvider, Set<TriodosScope> availableScopes) {
        this.scopesSupplier =
                new ScopesSupplier<>(
                        ItemsSupplier.get(componentProvider.getCredentialsRequest()),
                        availableScopes,
                        itemToScopes);
    }

    public static TriodosConsentGenerator of(
        AgentComponentProvider componentProvider, Set<TriodosScope> availableScopes) {
        return new TriodosConsentGenerator(componentProvider, availableScopes);
    }


    @Override
    public AccessEntity generate() {

        Set<TriodosScope> scopes = scopesSupplier.get();

        if (scopes.contains(TriodosScope.TRANSACTIONS)) {
            return new AccessEntity.Builder().build();
        } else if (scopes.contains(TriodosScope.BALANCES) || scopes.contains(TriodosScope.ACCOUNTS)) {
            return new AccessEntity.Builder().withoutTransactions().build();
        }
        return new AccessEntity.Builder().build();
    }
}
